package com.example.SocialMediaApp.Storage;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageService {

    private final WebClient webClient;
    private final StorageProperties storageEnv;


    // profile avatar uploading is done directly via the server
    public void uploadFile(MultipartFile file,String filepath) throws IOException {

        String bucket=storageEnv.getMediaBucket();

        ResponseEntity<String> response= webClient.put().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).
                header(HttpHeaders.CONTENT_TYPE, file.getContentType()).
                bodyValue(file.getBytes()).retrieve().toEntity(String.class).block();

        if (response==null||!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

    }

    public void deleteFile(String filepath){
        String bucket=storageEnv.getMediaBucket();
        webClient.delete().uri("/storage/v1/object/{bucket}/{filename}", bucket, filepath).retrieve().toBodilessEntity().block();
    }


    public void moveFiles(List<String> filePaths,StorageTransfer storageTransfer) {
        filePaths.forEach(oldPath -> {
            String newPath = oldPath.replace(storageTransfer.getSource(), storageTransfer.getDestination());
            webClient.post().uri("/storage/v1/object/move").contentType(MediaType.APPLICATION_JSON).bodyValue(Map.of(
                    "bucketId", storageEnv.getMediaBucket(),
                    "sourceKey", oldPath,
                    "destinationKey", newPath
            )).retrieve().toBodilessEntity().block();
        });
    }

    // used to generate a temporary signed url that the client can use to upload files
    public String generateSignedUrl(String filepath){
        String bucket=storageEnv.getMediaBucket();
        SignRequest signRequest=new SignRequest(5);
        String uri = "/storage/v1/object/upload/sign/" + bucket + "/" + filepath;
        log.info("Generating Supabase signed Url : "+uri);
        String signedUri=storageEnv.getUrl()+webClient.post().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signRequest).retrieve().bodyToMono(signResponse.class).map(signResponse::getUrl).block();
        return signedUri.replace(
                storageEnv.getUrl() + "/object",
                storageEnv.getUrl() + "/storage/v1/object"
        );
    }



}
