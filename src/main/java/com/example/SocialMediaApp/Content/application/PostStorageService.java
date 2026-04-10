package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostStorageService {
    private final StorageService storageService;
    private final StorageTransferManager storageTransferManager;

    public void transferFiles(String destinationFolder, List<String> filePaths,
                              StorageTransferManager.StorageTransfer storageTransfer) {
        storageService.transferTemporaryContent(destinationFolder, filePaths, storageTransfer);
    }

    public void moveFiles(String folderPath, StorageTransferManager.StorageTransfer storageTransfer) {
        storageService.moveBatchFiles(folderPath, storageTransfer);
    }

    public void deleteFiles(List<String> filePaths, Post.PostStatus status) {
        storageService.deleteFiles(filePaths, storageTransferManager.resolveBucket(status));
    }

    public String resolveNewPath(Post post, StorageTransferManager.StorageTransfer storageTransfer) {
        return post.getPostFolderPath().replace(
                storageTransfer.getSourceDir().getDirName(),
                storageTransfer.getDestinationDir().getDirName());
    }
}
