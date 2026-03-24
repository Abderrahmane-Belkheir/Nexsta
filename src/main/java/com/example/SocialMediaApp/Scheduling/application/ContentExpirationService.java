package com.example.SocialMediaApp.Scheduling.application;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ContentExpirationService {

    private final PostRepo postRepo;
    private final StoryRepo storyRepo;
    private final MediaRepo mediaRepo;

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredPosts(){
        Instant thirtyDaysAgo= Instant.now().minus(30, ChronoUnit.DAYS);
        Post.PostStatus postStatus= Post.PostStatus.DELETED;
        mediaRepo.deleteMediaBelongingToDeletedPosts(postStatus,thirtyDaysAgo);
        postRepo.deleteByOldPostsWithStatus(postStatus,thirtyDaysAgo);
}

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteAbandonedPosts(){
        Instant nintyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);
        Post.PostStatus postStatus= Post.PostStatus.DRAFT;
        mediaRepo.deleteMediaBelongingToDeletedPosts(postStatus, nintyDaysAgo);
        postRepo.deleteByOldPostsWithStatus(postStatus, nintyDaysAgo);
    }


    @Scheduled(cron = "0 0 4 * * *")
    public void deleteOldStoryViews(){

    }

}
