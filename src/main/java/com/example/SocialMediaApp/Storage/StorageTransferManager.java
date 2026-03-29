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

     BucketTransfer resolveBucketTransfer(StorageTransfer storageTransfer){
        StorageDir sourceDir=storageTransfer.getSourceDir();
        StorageDir destinationDir=storageTransfer.getDestinationDir();
        if(sourceDir==StorageDir.PERMANENT) return publicToPrivate;
        if(destinationDir==StorageDir.PERMANENT) return privateToPublic;
        return privateToPrivate;
    }

    public StorageTransfer resolveStorageTransfer(Post.PostStatus currentPostStatus, Post.PostStatus targetPostStatus){
        StorageDir sourceDir= resolveStorageDir(currentPostStatus);
        StorageDir destinationDir= resolveStorageDir(targetPostStatus);
        return new StorageTransfer(sourceDir,destinationDir);
    }
    private StorageDir resolveStorageDir(Post.PostStatus status){
        if(status==null) return StorageDir.TEMPORARY;
        return switch (status){
            case DELETED -> StorageDir.DELETED;
            case PUBLISHED,UNPUBLISHED -> StorageDir.PERMANENT ;
            case DRAFT,SCHEDULED -> StorageDir.DRAFT;
        };
    }

    public Map<String,String> resolveDestinationPaths(List<String> filePaths, StorageTransfer storageTransfer){
        return filePaths.stream().collect(
                Collectors.toMap(filePath-> filePath,
                        filePath->filePath.replace(storageTransfer.getSourceDir().getDirName(),storageTransfer.getDestinationDir().getDirName())));
    }

    //those are meant for internal use only inside the storage transfer manager you cannot create them outside
    @Getter
    public static class BucketTransfer {
        private final String bucketId;
        private final String destinationBucket;

        private BucketTransfer(String bucketId,String destinationBucket){
            this.bucketId=bucketId;
            this.destinationBucket=destinationBucket;
        }
    }


    @Getter
    public static class StorageTransfer {


        private final StorageDir sourceDir;
        private final StorageDir destinationDir;

        private StorageTransfer(StorageDir sourceDir,StorageDir destinationDir){
            this.sourceDir=sourceDir;
            this.destinationDir=destinationDir;
        }

    }


}
