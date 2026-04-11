package com.example.SocialMediaApp.Storage;

import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StorageTransferManager {

    private final StorageProperties storageProperties;
    private BucketTransfer publicToPrivate;
    private BucketTransfer privateToPublic;
    private BucketTransfer privateToPrivate;

    @PostConstruct
    public void init(){
        this.publicToPrivate=new BucketTransfer(storageProperties.getPublicMediaBucket(),storageProperties.getPrivateMediaBucket());
        this.privateToPublic=new BucketTransfer(storageProperties.getPrivateMediaBucket(),storageProperties.getPublicMediaBucket());
        this.privateToPrivate=new BucketTransfer(storageProperties.getPrivateMediaBucket(),storageProperties.getPrivateMediaBucket());
    }

    public BucketTransfer resolveBucketTransfer(StorageTransfer storageTransfer){
        if(storageTransfer.sourceDir==StorageDir.PERMANENT_PUBLIC) return publicToPrivate;
        if(storageTransfer.destinationDir==StorageDir.PERMANENT_PUBLIC) return privateToPublic;
        return privateToPrivate;
    }

    public Bucket resolveBucket(Post.PostStatus postStatus){
        if(postStatus== Post.PostStatus.PUBLISHED) return Bucket.PUBLIC;
        return Bucket.PRIVATE;
    }

    public StorageTransfer resolveStorageTransfer(Post.PostStatus currentPostStatus, Post.PostStatus targetPostStatus){
        StorageDir sourceDir= resolveStorageDir(currentPostStatus);
        StorageDir destinationDir= resolveStorageDir(targetPostStatus);
        return new StorageTransfer(sourceDir,destinationDir);
    }

    public StorageDir resolveStorageDir(Post.PostStatus status){
        if(status==null) return StorageDir.TEMPORARY;
        return  switch (status) {
            case DELETED -> StorageDir.DELETED;
            case PUBLISHED -> StorageDir.PERMANENT_PUBLIC;
            case UNPUBLISHED -> StorageDir.PERMANENT;
            case DRAFT, SCHEDULED -> StorageDir.DRAFT;
        };
    }

    public String resolveDestinationFolder(String sourceFolder,StorageTransfer storageTransfer){
        return sourceFolder.replace(storageTransfer.sourceDir.getDirName(),storageTransfer.destinationDir.getDirName());
    }
    //those are meant for internal use only inside the storage transfer manager you cannot create them outside
    @Getter
    public static class StorageTransfer {


        private final StorageDir sourceDir;
        private final StorageDir destinationDir;

        private StorageTransfer(StorageDir sourceDir,StorageDir destinationDir){
            this.sourceDir=sourceDir;
            this.destinationDir=destinationDir;
        }

    }

    @Getter
    public static  class BucketTransfer{
        private final String bucketId;
        private final String destinationBucket;
        private BucketTransfer(String bucketId,String destinationBucket){
            this.bucketId=bucketId;
            this.destinationBucket=destinationBucket;
        }
    }

    public enum Bucket{PRIVATE,PUBLIC}

}
