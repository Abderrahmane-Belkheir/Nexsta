
package com.example.SocialMediaApp.Scheduling.Jobs;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class PublishPostJob implements Job {

    private final PostRepo postRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap=context.getJobDetail().getJobDataMap();
        String postId=jobDataMap.getString("postId");
        log.info("Switching the post status for post : "+postId+" from scheduled to published");
        postRepo.updateScheduledPost(postId,context.getFireTime().toInstant());
    }

}
