
package com.example.SocialMediaApp.Scheduling.Jobs;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Storage.StorageService;
import com.example.SocialMediaApp.Storage.StorageTransferManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class PublishPostJob implements Job {

    private final PostRepo postRepo;
    private final StorageService storageService;
    private final StorageTransferManager storageTransferManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap=context.getJobDetail().getJobDataMap();
        String postId=jobDataMap.getString("postId");
        log.info("Switching the post status for post : "+postId+" from scheduled to published");
        Optional<Post> optionalPost=postRepo.findByIdAndPostStatusWithMediaList(postId,Post.PostStatus.SCHEDULED);
        if(optionalPost.isEmpty()){
            log.error("");
            return;
        }
        Post post=optionalPost.get();
        StorageTransferManager.StorageTransfer storageTransfer=storageTransferManager.resolveStorageTransfer(Post.PostStatus.SCHEDULED, Post.PostStatus.PUBLISHED);
        storageService.moveBatchFiles(post.getPostFolderPath(),storageTransfer);
        List<Media> mediaList= post.getMediaList();
        mediaList.forEach(media -> media.transformFilePath(storageTransfer));
        postRepo.save(post);
    }

}
