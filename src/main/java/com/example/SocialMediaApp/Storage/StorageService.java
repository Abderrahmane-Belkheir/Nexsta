package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Storage.Dto.MoveFolderRequest;
import com.example.SocialMediaApp.Storage.Dto.MoveFolderResponse;
import com.example.SocialMediaApp.Storage.Dto.MoveTemporaryContentRequest;
import com.example.SocialMediaApp.Storage.Dto.test;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageProperties;
    private final StorageTransferManager storageTransferManager;

    // profile avatar uploading is done directly via the server
    public void uploadFile(MultipartFile file,String filepath) throws IOException {

        String bucket= storageProperties.getPublicMediaBucket();

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

    }

    public void deleteFile(String filePath){
        String bucket= storageProperties.getPublicMediaBucket();
        webClient.delete().uri(storageProperties.getEndpoint()+"/{bucket}/{filename}", bucket, filePath).retrieve().toBodilessEntity().block();
    }

    public void deleteFiles(List<String> filePaths){
       filePaths.forEach(this::deleteFile);
    }


    // this method move files between the the same bucket which the private one
    public void renameFolder(String sourceFolder, StorageTransferManager.StorageTransfer storageTransfer) {
            String destinationFolder=storageTransferManager.resolveDestinationFolder(sourceFolder,storageTransfer);
            MoveFolderRequest request=MoveFolderRequest.builder().sourceFolder(sourceFolder).destinationFolder(destinationFolder).build();
            try {
                ResponseEntity<MoveFolderResponse> response=webClient.post().uri(storageProperties.getMoveInSameBucketsEndpoint()).contentType(MediaType.APPLICATION_JSON).
                        bodyValue(request).retrieve().toEntity(MoveFolderResponse.class).block();


            }catch (Exception e){

            }
    }

    //
    public void transferTemporaryContent( String destinationFolder,List<String> filePaths){
        MoveTemporaryContentRequest request=new MoveTemporaryContentRequest(destinationFolder,filePaths);
        try{
          ResponseEntity<Void> response= webClient.post().uri(storageProperties.getMoveBatchTempFilesEndpoint()).contentType(MediaType.APPLICATION_JSON).
                    bodyValue(request).retrieve().toBodilessEntity().block();

        }catch (Exception e){

        }
    }

    public void transferBetweenBuckets(String sourceFolder, StorageTransferManager.StorageTransfer storageTransfer){
       StorageTransferManager.BucketTransfer bucketTransfer= storageTransferManager.resolveBucketTransfer(storageTransfer);
       String destinationFolder=storageTransferManager.resolveDestinationFolder(sourceFolder,storageTransfer);
       MoveFolderRequest request=MoveFolderRequest.builder().bucketId(bucketTransfer.getBucketId()).sourceFolder(sourceFolder).destinationFolder(destinationFolder).destinationBucket(bucketTransfer.getDestinationBucket()).build();
       log.info(request.toString());
       try{
           ResponseEntity<test> response=webClient.post().uri(storageProperties.getMoveBetweenBucketsEndpoint()).contentType(MediaType.APPLICATION_JSON).
                   bodyValue(request).retrieve().toEntity(test.class).block();
           log.info(" response status "+response.getStatusCode());
           log.info("files moved = "+response.getBody().getFileMoved());

       }catch (Exception e){
           log.info(e.getMessage());
       }
    }

    /*
     used to generate a temporary signed url that the client can use to upload files
    */
    public String generateSignedUrl(String filePath){
        String bucket= storageProperties.getPrivateMediaBucket();
        SignedUploadRequest signRequest=new SignedUploadRequest(5);
        String signedUri= storageProperties.getUrl()+webClient.post().uri(uriBuilder -> uriBuilder.path(storageProperties.getEndpoint()+"/upload/sign/{bucket}/{path}").build(bucket, filePath))
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
           return null;
       }

        return response.stream().collect(Collectors.toMap(SignedFetchResponse::getPath,signedFetchResponse -> fullUrlBuilder(signedFetchResponse.getSignedURL())));
    }

    private String fullUrlBuilder(String url){
        return url.replace(
                storageProperties.getUrl() + "/object",
                storageProperties.getUrl() + "/storage/v1/object");
    }


}
