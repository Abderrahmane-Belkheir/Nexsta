package com.Nexsta.Shared.Mappers;

import com.Nexsta.Content.api.dto.*;
import com.Nexsta.Content.domain.Comment;
import com.Nexsta.Content.domain.Post;
import com.Nexsta.Content.domain.PostSettings;
import com.Nexsta.Content.domain.Story;
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
    @Mapping(target = "createdAt",ignore = true)
    PostRepresentation toPostRepresentation(Post post);

    @Mapping(target = "likes",ignore = true)
    @Mapping(target = "comments",ignore = true)
    @Mapping(target = "postPreview",ignore = true)
    PostPreviewRepresentation toPostPreview(Post post);

    CommentRepresentation toCommentRepresentation(Comment comment);

    PostUpdateResponse toPostUpdateResponse(Post post);

    @Mapping(target = "storyStatus",ignore = true)
    StoryRepresentation toStoryRepresentation(Story story);
}

