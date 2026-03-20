package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentExpirationService {

    private final PostRepo postRepo;
    private final StoryRepo storyRepo;
    private final MediaRepo mediaRepo;

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredPosts(){
        //postRepo.deleteByPostStatus(Post.PostStatus.DELETED);
    }


    @Scheduled(cron = "0 0 3 * * *")
    public void deleteAbandonedPosts(){

    }


    @Scheduled(cron = "0 0 4 * * *")
    public void deleteOldStoryViews(){

    }

}
