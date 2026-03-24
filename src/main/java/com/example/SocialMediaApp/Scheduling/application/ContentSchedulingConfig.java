package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Scheduling.Jobs.PublishPostJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentSchedulingConfig {

    @Bean
    JobDetail jobDetail(){
        return JobBuilder.newJob(PublishPostJob.class).withIdentity("publish-post").withDescription("Publishing Scheduled Posts").storeDurably().build();
    }

}
