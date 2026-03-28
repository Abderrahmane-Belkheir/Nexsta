package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StorageTransferManager {

    private  final StorageProperties storageProperties;
    private  BucketTransfer privateToPublic;
    private  BucketTransfer publicToPrivate;
    private  BucketTransfer privateToPrivate;

    @PostConstruct
    public void init(){
        this.privateToPublic =new BucketTransfer(storageProperties.getPrivateMediaBucket(),storageProperties.getPublicMediaBucket());
        this.publicToPrivate =new BucketTransfer(storageProperties.getPublicMediaBucket(),storageProperties.getPrivateMediaBucket());
        this.privateToPrivate=new BucketTransfer(storageProperties.getPrivateMediaBucket(),storageProperties.getPrivateMediaBucket());
    }

    public BucketTransfer resolveBucketTransfer(StorageTransfer storageTransfer){
        StorageDir sourceDir=storageTransfer.getSource();
        StorageDir destinationDir=storageTransfer.getDestination();
        if(sourceDir==StorageDir.DRAFT&&destinationDir==StorageDir.PERMANENT) return privateToPublic;
        if(sourceDir==StorageDir.PERMANENT&&destinationDir==StorageDir.DRAFT) return publicToPrivate;
        return privateToPrivate;
    }

    public StorageDir resolveSourceDir(Post.PostStatus postStatus){
        return switch (postStatus){
            case DRAFT,SCHEDULED -> StorageDir.DRAFT;
            case PUBLISHED,UNPUBLISHED -> StorageDir.PERMANENT;
            case DELETED -> StorageDir.DELETED;
        };
    }

    public Map<String,String> resolveDestinationPaths(List<String> filePaths, StorageTransfer storageTransfer){
        return filePaths.stream().collect(
                Collectors.toMap(filePath-> filePath,
                        filePath->filePath.replace(storageTransfer.getSource().getDirName(),storageTransfer.getDestination().getDirName())));
    }

    @Getter
    public static class BucketTransfer {
        private final String bucketId;
        private final String destinationBucket;

        private BucketTransfer(String bucketId,String destinationBucket){
            this.bucketId=bucketId;
            this.destinationBucket=destinationBucket;
        }
    }

}
