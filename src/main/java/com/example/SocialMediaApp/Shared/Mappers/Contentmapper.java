package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.domain.*;
import com.example.SocialMediaApp.Shared.MediaUrlResolver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Contentmapper {

    PostSettings toPostSettings(PostCreationRequest postCreation);

    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "postStatus" ,ignore = true)
    @Mapping(target = "mediaList",ignore = true)
    @Mapping(target = "restored" ,ignore = true)
    PostRepresentation toPostRepresentation(Post post);

    CommentRepresentation toCommentRepresentation(Comment comment);

    @Mapping(target = "filepath",ignore = true)
    MediaRepresentation toMediaRepresentation(Media media);
    @Mapping(target = "storyStatus",ignore = true)
    StoryRepresentation toStoryRepresentation(Story story);
}

