package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Notification.application.ContentNotificationService;
import com.example.SocialMediaApp.Notification.domain.EmailSending;
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
    private final ContentNotificationService contentNotificationService;


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
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (SchedulerException e) {
                    log.error("");
                }
            }
        });
        EmailSending emailSending=EmailSending.builder().build();
        contentNotificationService.sendEmail(emailSending);
        log.info("Scheduling done for post {}", postId);
    }

    public void unSchedulePostPublishing(String postId) throws SchedulerException {
        JobKey jobKey=JobKey.jobKey("schedule-publish-post"+postId);
        if(scheduler.deleteJob(jobKey)) log.info("unScheduling post : "+postId);
        else log.warn("failed unScheduling post : {}", postId);
    }


}
