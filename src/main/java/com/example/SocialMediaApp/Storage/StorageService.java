package com.example.SocialMediaApp.Storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;
    private final StorageTransferManager storageTransferManager;

    // profile avatar uploading is done directly via the server
    public void uploadFile(MultipartFile file,String filepath) throws IOException {

        String bucket=storageEnv.getPublicMediaBucket();

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

    }

    public void deleteFile(String filePath){
        String bucket=storageEnv.getPublicMediaBucket();
        webClient.delete().uri("/storage/v1/object/{bucket}/{filename}", bucket, filePath).retrieve().toBodilessEntity().block();
    }

    public void deleteFiles(List<String> filePaths){
       filePaths.forEach(this::deleteFile);
    }


    @Async
    public CompletableFuture<Void> moveFiles(List<String> filePaths, StorageTransferManager.StorageTransfer storageTransfer) {
        StorageTransferManager.BucketTransfer bucketTransfer=storageTransferManager.resolveBucketTransfer(storageTransfer);
        Map<String,String> sourceToDestinationPaths=storageTransferManager.resolveDestinationPaths(filePaths,storageTransfer);
        for (Map.Entry<String,String> entry: sourceToDestinationPaths.entrySet()){
            String sourceKey=entry.getValue();
            String destinationKey=entry.getValue();
            webClient.post().uri("/move").contentType(MediaType.APPLICATION_JSON).
                    bodyValue(new MoveFileRequest(bucketTransfer.getBucketId(),sourceKey,bucketTransfer.getDestinationBucket(),destinationKey)).retrieve().toBodilessEntity().block();

    }
        return CompletableFuture.completedFuture(null);
    }

    /*
     used to generate a temporary signed url that the client can use to upload files
    */
    public String generateSignedUrl(String filePath){
        String bucket=storageEnv.getPrivateMediaBucket();
        SignedUploadRequest signRequest=new SignedUploadRequest(5);
        String signedUri=storageEnv.getUrl()+webClient.post().uri(uriBuilder -> uriBuilder.path("/upload/sign/{bucket}/{path}").build(bucket, filePath))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signRequest).retrieve().bodyToMono(signedUploadResponse.class).map(signedUploadResponse::getUrl).block();

       return fullUrlBuilder(signedUri);
    }

    /*
    used to generate signed urls for media inside the private media bucket specifically for unpublished posts
    */
    public Map<String,String>  generateBatchFetchSignedUrls(List<String> filePaths){

        String bucket=storageEnv.getPrivateMediaBucket();

       SignedFetchRequest request=new SignedFetchRequest(filePaths,5);

       List<SignedFetchResponse> response= webClient.post().uri(uriBuilder -> uriBuilder.path("/sign/{bucket}").
                       build(bucket)).contentType(MediaType.APPLICATION_JSON).bodyValue(request).
               retrieve().bodyToFlux(SignedFetchResponse.class).collectList().block();

       if(response==null||response.isEmpty()){
           return null;
       }

        return response.stream().collect(Collectors.toMap(SignedFetchResponse::getPath,signedFetchResponse -> fullUrlBuilder(signedFetchResponse.getSignedURL())));
    }

    private String fullUrlBuilder(String url){
        return url.replace(
                storageEnv.getUrl() + "/object",
                storageEnv.getUrl() + "/storage/v1/object");
    }


}
