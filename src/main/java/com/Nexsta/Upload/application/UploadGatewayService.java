package com.Nexsta.Upload.application;


import com.Nexsta.Content.domain.Post;
import com.Nexsta.Storage.StorageService;
import com.Nexsta.Storage.StorageTransferManager;
import com.Nexsta.Upload.Exceptions.*;
import com.Nexsta.Upload.api.dto.UploadRequest;
import com.Nexsta.Upload.api.dto.UploadResponse;
import com.Nexsta.Upload.domain.*;
import com.Nexsta.Upload.Exceptions.*;
import com.Nexsta.Upload.api.dto.*;
import com.Nexsta.Upload.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class
UploadGatewayService {

    private final StorageService storageService;
    private final RedisTemplate<String,Object> objectRedisTemplate;
    private final RedisTemplate<String,String> redisTemplate;
    private final UploadStateService uploadStateService;
    private final UploadUtil uploadUtil;
    private final UploadValidationService uploadValidationService;
    private final WebhookVerification webhookVerification;
    private final StorageTransferManager storageTransferManager;
    private static final int UPLOAD_WAIT_DURATION_MINUTES = 5;
    private static final int UPLOAD_CONFIRM_DURATION_MINUTES = 5;


    // used to upload files directly though the server
    public String Upload(MultipartFile file, String userId) throws IOException {
        UploadRequest uploadRequest= uploadUtil.toUploadRequest(file);
        uploadValidationService.validateFile(uploadRequest);
        UploadInitiation uploadInitiation= uploadUtil.generateUploadResponse(userId,uploadRequest);
        String filepath=uploadInitiation.getFilepath();
        // for this method since the uploading is done directly via the server
        // don't need to make the file start with temporary to not get deleted later by the cron job
        storageService.uploadFile(file,filepath.replace("temporary","permanent"));
        return filepath;
    }

    public UploadResponse requestUpload(String userId, UploadRequest request){
         uploadValidationService.validateFile(request);
         UploadInitiation uploadInitiation=uploadUtil.generateUploadResponse(userId,request);
         UploadType uploadType=request.getUploadType();
         String filepath=uploadInitiation.getFilepath();
         String uploadRequestId=uploadInitiation.getUploadRequestId();
         String signedUrl=storageService.generateSignedUrl(filepath);

         // creating an upload session containing the user id for authorization later + the request id and the upload type
        UploadSession uploadSession=UploadSession.builder()
                .userId(userId).uploadType(uploadType).
                uploadRequestId(uploadRequestId)
                .filePath(filepath).build();
        objectRedisTemplate.opsForValue().set(filepath,uploadSession,UPLOAD_WAIT_DURATION_MINUTES,TimeUnit.MINUTES);
        return new UploadResponse(signedUrl,uploadRequestId);
    }

    public void confirmUpload(String signature, SupabaseWebhookPayload webhookPayload){

        String filePath=null;
        webhookVerification.verifySignature(signature);

        try{

            filePath=webhookPayload.getRecord().getName();

            UploadSession uploadSession=uploadStateService.validateUploadSession("",filePath, UploadPhase.REQUESTED);

            webhookVerification.verifyFileUploaded(uploadSession,webhookPayload.getRecord());

            String uploadRequestId=uploadSession.getUploadRequestId();

            if(uploadRequestId==null) throw new UploadSessionExpiredException("");

            objectRedisTemplate.opsForValue().set(uploadSession.getUploadRequestId(),uploadSession,UPLOAD_CONFIRM_DURATION_MINUTES, TimeUnit.MINUTES);

        }catch (UploadSessionExpiredException | UnsupportedMediaTypeException | FileTooLargeException e){

            storageService.deleteFile(filePath,storageTransferManager.resolveBucket(Post.PostStatus.DRAFT));

            if(e instanceof  UnsupportedMediaTypeException || e instanceof FileTooLargeException ){
                // might block user later for bypassing the request phase filter
            }

        }

    }

    public void discardUpload(String userId, String uploadRequestId){

        // upload state must confirmed to delete the file
        UploadSession uploadSession =uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);
        storageService.deleteFile(uploadSession.getFilePath(),storageTransferManager.resolveBucket(Post.PostStatus.DRAFT));
        redisTemplate.delete(uploadSession.getUploadRequestId());
    }

    public UploadSession finalizeUpload(String userId,String uploadRequestId,UploadType uploadType){
        UploadSession uploadSession=uploadStateService.validateUploadSession(userId,uploadRequestId, UploadPhase.CONFIRMED);
        UploadType actualUploadType=uploadSession.getUploadType();
        if(actualUploadType!=uploadType) throw new UploadTypeMismatch("Upload Type Mismatch");
        return uploadSession;
    }

    public UploadFinalization finalizeUploads(String userId, List<String> uploadRequestsIds, UploadType uploadType){

        List<String> filesPaths =new ArrayList<>();
        List<MediaUpload> mediaList=new ArrayList<>();
        List<String> failedUploadIds=new ArrayList<>();

        for(String uploadRequestId : uploadRequestsIds){
            try{
                UploadSession uploadSession=finalizeUpload(userId,uploadRequestId,uploadType);
                String filepath=uploadSession.getFilePath();
                filesPaths.add(filepath);
                log.info("media type is "+uploadSession.getMediaType());
                mediaList.add(new MediaUpload(uploadRequestId, uploadSession.getMediaType()));
                // upload session expired which mean the key is not found in redis is the only recoverable case
            }catch (UploadSessionExpiredException e){
                failedUploadIds.add(uploadRequestId);
            }catch (UnauthorizedResourceAccessException e){
                // logging and blocking user later
                throw e;
            }

        }

        if(!failedUploadIds.isEmpty()) throw new UploadFailedException(failedUploadIds);

        objectRedisTemplate.delete(uploadRequestsIds);

        return new UploadFinalization(mediaList,filesPaths);
    }




}
