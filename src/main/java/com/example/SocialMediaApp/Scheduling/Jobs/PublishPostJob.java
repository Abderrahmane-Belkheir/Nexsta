
package com.example.SocialMediaApp.Scheduling.Jobs;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

@RequiredArgsConstructor
public class PublishPostJob implements Job {

    private final PostRepo postRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap=context.getTrigger().getJobDataMap();
        String postId=jobDataMap.getString("postId");
        String userId=jobDataMap.getString("userId");
        postRepo.updatePostStatus(postId, Post.PostStatus.PUBLISHED,userId,List.of(Post.PostStatus.SCHEDULED));
    }
}
