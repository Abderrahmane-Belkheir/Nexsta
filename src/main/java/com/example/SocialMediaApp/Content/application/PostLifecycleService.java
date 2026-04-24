package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Upload.domain.UploadFinalization;
import com.example.SocialMediaApp.Upload.domain.UploadType;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import com.example.SocialMediaApp.User.persistence.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostLifecycleService {

    private final AuthenticatedUserService authenticatedUserService;
    private final PostRepo postRepo;
    private final MediaLifecycleService mediaLifecycleService;
    private final Contentmapper contentmapper;
    private final PostStorageService postStorageService;
    private final ThumbnailService thumbnailGenerator;
    private final PostSchedulingService postSchedulingService;

    public PostRepresentation createPost(PostCreationRequest request) throws SchedulerException {
        String currentUserId = authenticatedUserService.getCurrentUser();
        Post.PostStatus status = getPostStatus(request.getPostAction());
        if (status == Post.PostStatus.DRAFT) validateDraftLimit(currentUserId);

        UploadFinalization uploadFinalization = mediaLifecycleService.extractMediaUploads(
                currentUserId, request.getUploadRequestsIds(), UploadType.POST);

        String postId = UUID.randomUUID().toString();
        String destinationFolder = postStorageService.resolveDestinationFolder(currentUserId, postId, status);

        Post post = Post.builder()
                .id(postId).postStatus(status).user(new User(currentUserId))
                .caption(request.getCaption()).postSettings(request.getPostSettings())
                .location(request.getLocation()).postFolderPath(destinationFolder).scheduledAt(request.getScheduleAt())
                .build();

        if(request.getPostAction()== PostCreationRequest.PostAction.PUBLISHED) post.setPublishedAt(Instant.now());

        List<Media> mediaList = mediaLifecycleService.persistMedia(uploadFinalization.getMediaUploads(), post);
        PostPreview postPreview=thumbnailGenerator.generatePostThumbnail(currentUserId,request.getThumbnailRequestId(),mediaList.get(0));
        post.setPostPreview(postPreview);
        postRepo.save(post);
        List<String> filesPath= uploadFinalization.getFilePaths();
        filesPath.add(postPreview.getThumbnailFilePath());
        postStorageService.transferTemporaryFiles(destinationFolder,filesPath, status);

        if (status == Post.PostStatus.SCHEDULED){
            postSchedulingService.schedulePost(postId,request.getScheduleAt());
        }

        PostRepresentation rep = contentmapper.toPostRepresentation(post);
        rep.setPostStatus(status);
        return rep;
    }

    public DeletePostResponse deletePost(String postId) throws SchedulerException {
        String currentUserId = authenticatedUserService.getCurrentUser();
        Post post = postRepo.findByIdAndUserIdWithMediaList(currentUserId,postId)
                .orElseThrow(() -> new ContentNotFoundException("Post to Delete Not Found"));

        if (post.getPostStatus() == Post.PostStatus.DELETED)
            throw new ActionNotAllowedException("Post Already Deleted");

        if (post.getPostStatus() == Post.PostStatus.SCHEDULED)
            postSchedulingService.unScheduleJob(postId);

        if (post.isRestored()) {
            try {
                postStorageService.deletePostFiles(post);
            } catch (Exception e) {
                log.error("Failed to delete files from storage for post {}: {}", postId, e.getMessage());
            }
            postRepo.delete(post);
            return new DeletePostResponse(false);
        }

        Post.PostStatus sourceStatus = post.getPostStatus();
        post.setPreDeletionStatus(sourceStatus == Post.PostStatus.SCHEDULED ? Post.PostStatus.DRAFT : sourceStatus);
        post.setPostStatus(Post.PostStatus.DELETED);
        post.setDeletedAt(Instant.now());
        post.setScheduledAt(null);
        post.setPostFolderPath(postStorageService.moveAndResolvePath(post, sourceStatus, Post.PostStatus.DELETED));
        postRepo.save(post);
        return new DeletePostResponse(true);
    }

    public PostRepresentation restorePost(String postId) {
        String currentUserId = authenticatedUserService.getCurrentUser();
        Post post = postRepo.findPostToRestore(postId, currentUserId)
                .orElseThrow(() -> new ContentNotFoundException("Post to Restore Not Found"));

        Post.PostStatus destinationStatus = post.getPreDeletionStatus();
        post.setRestored(true);
        post.setPostStatus(destinationStatus);
        post.setPreDeletionStatus(null);
        post.setPostFolderPath(postStorageService.moveAndResolvePath(post, Post.PostStatus.DELETED, destinationStatus));
        postRepo.save(post);

        List<Media> mediaList = post.getMediaList();
        PostRepresentation rep = contentmapper.toPostRepresentation(post);
        rep.getMediaList().addAll(mediaList.stream()
                .map(m -> new MediaRepresentation(m.getId(), m.getMediaType())).toList());
        rep.setLikes(post.getLikeCount());
        rep.setComments(post.getCommentCount());
        rep.setRestored(true);
        rep.setPostStatus(post.getPostStatus());
        return rep;
    }

    // publishing post for first time draft -> published / scheduled
    public void publishPost(String postId,PostPublish postPublish) throws SchedulerException {
        String currentUserId=authenticatedUserService.getCurrentUser();

        Post draftPost=postRepo.findByIdAndUserIdAndPostStatusWithMediaList(currentUserId,postId, Post.PostStatus.DRAFT).
                orElseThrow(()-> new ContentNotFoundException("Post Not Found"));

        if(postPublish.getScheduledAt()!=null) {
            draftPost.setPostStatus(Post.PostStatus.SCHEDULED);
            draftPost.setScheduledAt(postPublish.getScheduledAt());
            postRepo.save(draftPost);
            postSchedulingService.schedulePost(postId,postPublish.getScheduledAt());
            return;
        }

        draftPost.setPublishedAt(Instant.now());
        draftPost.setPostStatus(Post.PostStatus.PUBLISHED);
        draftPost.setPostFolderPath(postStorageService.moveAndResolvePath(draftPost, Post.PostStatus.DRAFT, Post.PostStatus.PUBLISHED));
        postRepo.save(draftPost);
    }

    private Post.PostStatus getPostStatus(PostCreationRequest.PostAction postAction) {
        return switch (postAction) {
            case DRAFT -> Post.PostStatus.DRAFT;
            case PUBLISHED -> Post.PostStatus.PUBLISHED;
            case SCHEDULED -> Post.PostStatus.SCHEDULED;
        };
    }

    private void validateDraftLimit(String userId) {
        if (postRepo.isDraftLimitReached(userId, 10))
            throw new ActionNotAllowedException("Draft Post limit exceeded. Delete or publish some drafts first.");
    }
}


