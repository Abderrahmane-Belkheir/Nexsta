package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageTransferManager {

    private final StorageProperties storageProperties;

    public StorageDir resolveSourceDir(Post.PostStatus postStatus){
        return switch (){
            case
        }
    }

    public MoveFilesRequest getStorageTransfer(StorageDir sourceDir,StorageDir destinationDir){

        MoveFilesRequest request=new MoveFilesRequest();


        return null;
    }


    @NoArgsConstructor
    @Data
     public static class MoveFilesRequest {
        private String bucketId;
        private String sourceKey;
        private String destinationBucket;
        private String destinationKey;

        private MoveFilesRequest(String bucketId,String sourceKey,String destinationBucket,String destinationKey){
            this.bucketId=bucketId;
            this.sourceKey=sourceKey;
            this.destinationBucket=destinationBucket;
            this.destinationKey=destinationKey;
        }
    }

}
