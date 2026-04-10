package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Content.api.dto.PostPublish;
import com.example.SocialMediaApp.Scheduling.Jobs.PublishPostJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentSchedulingService {

    private final Scheduler scheduler;


    public void schedulePostPublishing(String postId, Instant date) throws SchedulerException {
        JobDetail jobDetail=JobBuilder.newJob(PublishPostJob.class).
                withIdentity("schedule-publish-post"+postId).
                withDescription("Publishing Scheduled Posts").storeDurably().
                usingJobData("postId",postId).
                build();
        Trigger trigger=TriggerBuilder.newTrigger().
                withIdentity("trigger-" + postId, "post-triggers")
                .startAt(Date.from(date)).forJob(jobDetail)
                .build();
        log.info("Scheduling post publishing for postId : "+postId+" At : "+date);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                    log.info("🚀 Quartz Job scheduled AFTER DB Commit for post: " + postId);
                } catch (SchedulerException e) {
                    log.error("Failed to schedule job", e);
                }
            }
        });
        log.info("Scheduling done");
    }

    public void unSchedulePostPublishing(String postId) throws SchedulerException {
        JobKey jobKey=JobKey.jobKey("schedule-publish-post"+postId);
        if(scheduler.deleteJob(jobKey)) log.info("unScheduling post : "+postId);
        else log.warn("failed unScheduling post : "+postId);
    }


}
