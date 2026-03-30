package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.MediaRepresentation;
import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.StoryRepresentation;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Story;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Upload.domain.MediaUpload;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryLifecycleService {

    private final MediaLifecycleService mediaLifecycleService;
    private final AuthenticatedUserService authenticatedUserService;
    private final StoryRepo storyRepo;
    private final Contentmapper contentmapper;


    public StoryRepresentation createStory(StoryCreationRequest storyCreationRequest){
        String currentUserId=authenticatedUserService.getCurrentUser();
        List<String> uploadRequestsIds=storyCreationRequest.getUploadRequestsIds();
        UploadFinalization uploadFinalization= mediaLifecycleService.extractMediaUploads(currentUserId,uploadRequestsIds, UploadType.STORY);
        Story story= storyRepo.save(Story.builder().user(new User(currentUserId))
                .storySettings(storyCreationRequest.getStorySettings()).build());
        List<Media> mediaList=mediaLifecycleService.persistMedia(uploadFinalization.getMediaUploads(),story);
        StoryRepresentation storyRepresentation=contentmapper.toStoryRepresentation(story);
        storyRepresentation.setStoryStatus(Story.StoryStatus.DRAFT);
        List<MediaRepresentation> mediaRepresentationList=mediaList.stream().map(contentmapper::toMediaRepresentation).toList();
        storyRepresentation.getMediaList().addAll(mediaRepresentationList);
        return storyRepresentation;
    }

    public void publishStory(String storyId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Story draftStory=storyRepo.findByIdAndUserIdAndStoryStatus(storyId, currentUserId, Story.StoryStatus.DRAFT).
                orElseThrow(()-> new ActionNotAllowedException("Action could not be completed"));
        draftStory.setPublishedAt(Instant.now());
        draftStory.setExpiresAt(Instant.now().plus(24,ChronoUnit.HOURS));
        draftStory.setStoryStatus(Story.StoryStatus.PUBLISHED);
        storyRepo.save(draftStory);
    }

    public void deleteStory(String storyId){
        String currentUserId=authenticatedUserService.getCurrentUser();
    }




}
