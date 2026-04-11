package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostStorageService {

    private final StorageService storageService;
    private final StorageTransferManager storageTransferManager;
    private final MediaLifecycleService mediaLifecycleService;
    private final MediaUrlResolver mediaUrlResolver;

    // resolve destination folder for a new post based on status
    public String resolveDestinationFolder(String userId, String postId, Post.PostStatus status) {
        StorageTransferManager.StorageTransfer transfer = storageTransferManager.resolveStorageTransfer(null, status);
        return mediaLifecycleService.buildFolderPath(transfer.getDestinationDir(),UploadType.POST,userId, postId);
    }

    // transfer temp files to post destination folder
    public void transferFiles(String destinationFolder, List<String> filePaths, Post.PostStatus status) {
        StorageTransferManager.StorageTransfer transfer = storageTransferManager.resolveStorageTransfer(null, status);
        storageService.transferTemporaryContent(destinationFolder, filePaths, transfer);
    }

    // move post folder from sourceStatus to destinationStatus and return the new path
    public String moveAndResolvePath(Post post, Post.PostStatus sourceStatus, Post.PostStatus destinationStatus) {
        StorageTransferManager.StorageTransfer transfer =
                storageTransferManager.resolveStorageTransfer(sourceStatus, destinationStatus);
        storageService.moveBatchFiles(post.getPostFolderPath(), transfer);
        return post.getPostFolderPath().replace(
                transfer.getSourceDir().getDirName(),
                transfer.getDestinationDir().getDirName());
    }

    // delete all files of a restored post directly from storage
    public void deletePostFiles(Post post) {
        List<String> filePaths = post.getMediaList().stream()
                .map(media -> mediaUrlResolver.resolvePath(post.getPostFolderPath(), media.getId()))
                .toList();
        storageService.deleteFiles(filePaths, storageTransferManager.resolveBucket(post.getPostStatus()));
    }
}