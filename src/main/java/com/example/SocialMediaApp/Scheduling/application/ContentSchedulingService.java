package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Content.api.dto.PostPublish;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ContentSchedulingService {

    private final Scheduler scheduler;
    private final JobDetail jobDetail;

    public void schedulePostPublishing(String userId,PostPublish postPublish) throws SchedulerException {
        Trigger trigger=TriggerBuilder.newTrigger()
                .startAt(Date.from(postPublish.getScheduledAt())).forJob(jobDetail)
                .usingJobData("userId",userId).usingJobData("postId",postPublish.getPostId()).build();
        scheduler.scheduleJob(jobDetail,trigger);
    }

    public void unSchedulePostPublishing(){

    }
}
