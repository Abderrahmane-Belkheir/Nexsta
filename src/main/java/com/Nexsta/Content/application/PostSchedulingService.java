package com.Nexsta.Content.application;

import com.Nexsta.Content.Exceptions.ContentNotFoundException;
import com.Nexsta.Content.domain.Post;
import com.Nexsta.Content.persistence.PostRepo;
import com.Nexsta.Scheduling.application.ContentSchedulingService;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PostSchedulingService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final ContentSchedulingService contentSchedulingService;


    public void schedulePost(String postId,Instant scheduledAt) throws SchedulerException {
        contentSchedulingService.schedulePostPublishing(postId,scheduledAt);
    }

    public void unSchedulePost(String postId) throws SchedulerException {
            String currentUserId=authenticatedUserService.getCurrentUser();
            Post post=postRepo.findByIdAndUserIdAndPostStatus(postId,currentUserId, Post.PostStatus.SCHEDULED).orElseThrow(()->new ContentNotFoundException("Post to unSchedule Not Found"));
            post.setScheduledAt(null);
            post.setPostStatus(Post.PostStatus.DRAFT);
            unScheduleJob(postId);
            postRepo.save(post);
    }

    public void unScheduleJob(String postId) throws SchedulerException {
        contentSchedulingService.unSchedulePostPublishing(postId);
    }
}
