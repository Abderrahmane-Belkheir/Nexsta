package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotAvailableException;
import com.example.SocialMediaApp.Content.api.dto.StoryRepresentation;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.MediaRepo;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.Content.persistence.StoryViewRepo;

import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryQueryService {

    private final StoryRepo storyRepo;
    private final MediaRepo mediaRepo;
    private final VisibilityPolicy visibilityPolicy;
    private final StoryViewRepo storyViewRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public Page<StoryRepresentation> getMyStories(){
        String currentUserId=authenticatedUserService.getCurrentUser();
        return getStoryRepresentation(currentUserId);
    }

    @CheckUserExistence
    public Page<StoryRepresentation> getUserStories(String targetId){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyStories();
        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("");
        List<Story> storyList= storyRepo.getUserActiveStories(targetId, Story.StoryStatus.PUBLISHED);
        return null;
    }

    private Page<StoryRepresentation> getStoryRepresentation(String userId){


        return null;
    }


}
