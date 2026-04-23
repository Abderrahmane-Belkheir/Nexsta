package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Storage.Dto.MoveFolderRequest;
import com.example.SocialMediaApp.Storage.Dto.MoveFolderResponse;
import com.example.SocialMediaApp.Storage.Dto.MoveTemporaryContentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageProperties;
    private final StorageTransferManager storageTransferManager;

    public StorageService(StorageProperties storageProperties, StorageTransferManager storageTransferManager, @Qualifier("storageWebClient") WebClient webClient){
        this.storageProperties=storageProperties;
        this.storageTransferManager=storageTransferManager;
        this.webClient=webClient;
    }
    // profile avatar uploading is done directly via the server
    public void uploadFile(MultipartFile file,String filepath) throws IOException {

        String bucket= storageProperties.getPublicMediaBucket();

        ResponseEntity<String> response= webClient.put().uri(storageProperties.getEndpoint()+"/{bucket}/{filename}", bucket, filepath).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

    }

    public void deleteFile(String filePath, StorageTransferManager.Bucket bucket){
        webClient.delete().
                uri(storageProperties.getEndpoint()+"/{bucket}/{filename}",
                        bucket== StorageTransferManager.Bucket.PUBLIC?storageProperties.getPublicMediaBucket():storageProperties.getPrivateMediaBucket(), filePath).retrieve().toBodilessEntity().block();
    }

    public void deleteFiles(List<String> filePaths, StorageTransferManager.Bucket bucket){
       filePaths.forEach(filePath->deleteFile(filePath,bucket));
    }



    public void moveBatchFiles(String sourceFolder, StorageTransferManager.StorageTransfer storageTransfer) {

            StorageTransferManager.BucketTransfer bucketTransfer=storageTransferManager.resolveBucketTransfer(storageTransfer);

            String destinationFolder=storageTransferManager.resolveDestinationFolder(sourceFolder,storageTransfer);

            MoveFolderRequest request=MoveFolderRequest.builder().sourceFolder(sourceFolder)
                    .bucketId(bucketTransfer.getBucketId()).destinationFolder(destinationFolder).
                    destinationBucket(bucketTransfer.getDestinationBucket()).build();
            log.info(request.toString());

            try {
                ResponseEntity<MoveFolderResponse> response= webClient.post().uri(storageProperties.getBatchFileMoveEndpoint()).contentType(MediaType.APPLICATION_JSON).
                        bodyValue(request).retrieve().toEntity(MoveFolderResponse.class).block();


            }catch (Exception e){

            }
    }


    public void transferTemporaryContent(String destinationFolder, List<String> filePaths, StorageTransferManager.StorageTransfer storageTransfer){
        StorageTransferManager.BucketTransfer bucketTransfer=storageTransferManager.resolveBucketTransfer(storageTransfer);
        MoveTemporaryContentRequest request=new MoveTemporaryContentRequest(bucketTransfer.getDestinationBucket(),destinationFolder,filePaths);
        try{
          ResponseEntity<Void> response= webClient.post().uri(storageProperties.getBatchTempFileMoveEndpoint()).contentType(MediaType.APPLICATION_JSON).
                    bodyValue(request).retrieve().toBodilessEntity().block();

        }catch (Exception e){

        }
    }

    /*
     used to generate a temporary signed url that the client can use to upload files
    */
    public String generateSignedUrl(String filePath){
        String bucket= storageProperties.getPrivateMediaBucket();
        SignedUploadRequest signRequest=new SignedUploadRequest(5);
        String signedUri= storageProperties.getUrl()+ webClient.post().uri(uriBuilder -> uriBuilder.path(storageProperties.getEndpoint()+"/upload/sign/{bucket}/{path}").build(bucket, filePath))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signRequest).retrieve().bodyToMono(signedUploadResponse.class).map(signedUploadResponse::getUrl).block();

       return fullUrlBuilder(signedUri);
    }

    /*
    used to generate signed urls for media inside the private media bucket specifically for unpublished posts
    */
    public Map<String,String>  generateBatchFetchSignedUrls(List<String> filePaths,int expiry){

        String bucket= storageProperties.getPrivateMediaBucket();

       SignedFetchRequest request=new SignedFetchRequest(filePaths,expiry);

       List<SignedFetchResponse> response= webClient.post().uri(uriBuilder -> uriBuilder.path(storageProperties.getEndpoint()+"/sign/{bucket}").build(bucket)).contentType(MediaType.APPLICATION_JSON).bodyValue(request).
               retrieve().bodyToFlux(SignedFetchResponse.class).collectList().block();

       if(response==null||response.isEmpty()){
           log.error("batch fetch signed Urls is empty or null");
           return new HashMap<>();
       }

        return response.stream().collect(Collectors.toMap(SignedFetchResponse::getPath, SignedFetchResponse::getSignedURL));
    }

    private String fullUrlBuilder(String url){
        return url.replace(
                storageProperties.getUrl() + "/object",
                storageProperties.getUrl() + "/storage/v1/object");
    }


}
