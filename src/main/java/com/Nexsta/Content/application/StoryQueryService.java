package com.Nexsta.Content.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Content.api.dto.StoryRepresentation;
import com.Nexsta.Content.domain.Story;
import com.Nexsta.Content.persistence.LikeRepo;
import com.Nexsta.Content.persistence.MediaRepo;
import com.Nexsta.Content.persistence.StoryRepo;
import com.Nexsta.Content.persistence.*;

import com.Nexsta.Profile.application.ProfileQueryService;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.Shared.Mappers.Contentmapper;
import com.Nexsta.Shared.VisibilityPolicy;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryQueryService {

    private final StoryRepo storyRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final ProfileQueryService profileQueryService;
    private final Contentmapper contentmapper;
    private final LikeRepo likeRepo;
    private final MediaRepo mediaRepo;

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
