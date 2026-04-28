package com.Nexsta.Content.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Content.api.dto.PostPreviewRepresentation;
import com.Nexsta.Content.api.dto.PostPreviewRequest;
import com.Nexsta.Content.api.dto.PostPreviewResponse;
import com.Nexsta.Content.domain.Post;
import com.Nexsta.Content.domain.PostPreview;
import com.Nexsta.Content.domain.PostSettings;
import com.Nexsta.Content.persistence.PostRepo;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.Shared.Mappers.Contentmapper;
import com.Nexsta.Shared.MediaUrlResolver;
import com.Nexsta.Shared.ViewerType;
import com.Nexsta.Shared.VisibilityPolicy;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
public class PostPreviewQueryService {
    private final PostRepo postRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final Contentmapper contentmapper;
    private final MediaUrlResolver mediaUrlResolver;
    private static final int postQueryLimit=10;


    public PostPreviewResponse getMyPostsPreview(PostPreviewRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();
        return getPostsPreviewRepresentation(currentUserId,request.getStatus(),request.getCursor(),ViewerType.OWNER);
    }

    @CheckUserExistence
    public PostPreviewResponse getUserPostsPreview(String targetId,PostPreviewRequest request){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(currentUserId.equals(targetId)) return getMyPostsPreview(request);

        if(!visibilityPolicy.isAllowed(currentUserId,targetId)) throw new ContentNotAvailableException("This content is not available");

        return getPostsPreviewRepresentation(targetId, Post.PostStatus.PUBLISHED,request.getCursor(),ViewerType.VIEWER);
    }

    private List<Post> fetchPosts(String userId,Post.PostStatus status,Instant cursor){
        if(status== Post.PostStatus.PUBLISHED||status== Post.PostStatus.UNPUBLISHED) return cursor==null?
                postRepo.findTop11ByUserIdAndPostStatusOrderByPublishedAtDesc(userId,status):
                postRepo.findTop11ByUserIdAndPostStatusAndPublishedAtBeforeOrderByPublishedAtDesc(userId,status,cursor);

        return cursor==null?
                postRepo.findTop11ByUserIdAndPostStatusOrderByCreatedAtDesc(userId,status):
                postRepo.findTop11ByUserIdAndPostStatusAndCreatedAtBeforeOrderByCreatedAtDesc(userId,status,cursor);
    }

    private PostPreviewResponse getPostsPreviewRepresentation(String userId, Post.PostStatus postStatus, Instant cursor, ViewerType viewerType){

        List<Post> postList=fetchPosts(userId,postStatus,cursor);
        boolean hasMore=postList.size()==postQueryLimit+1;
        if(hasMore) postList.remove(postQueryLimit);

        List<PostPreviewRepresentation> postPreviewRepresentationList= postList.stream().map(post->{

            PostPreviewRepresentation postPreviewRepresentation= contentmapper.toPostPreview(post);


                PostPreview postPreview=post.getPostPreview();

                if(postPreview!=null){
                    String thumbnailPath=mediaUrlResolver.resolvePath(post.getPostFolderPath(),postPreview.getThumbnail());
                    String thumbnailFullUrl=postStatus== Post.PostStatus.PUBLISHED?mediaUrlResolver.resolveFullUrl(thumbnailPath): mediaUrlResolver.generateSignerUrl(thumbnailPath);
                    postPreview.setThumbnail(thumbnailFullUrl);
                    postPreviewRepresentation.setPostPreview(postPreview);
                }


            PostSettings postSettings=post.getPostSettings();

            if(viewerType==ViewerType.VIEWER){

                if(!postSettings.isHideLikes()){
                    postPreviewRepresentation.setLikes(post.getLikeCount());
                }

                if(!postSettings.isHideComments()){
                    postPreviewRepresentation.setComments(post.getCommentCount());
                }

            }else{
                // likes and comments count can be seen by the owner directly.
                postPreviewRepresentation.setLikes(post.getLikeCount());
                postPreviewRepresentation.setComments(post.getCommentCount());
            }

            return postPreviewRepresentation;
        }).toList();

        Instant nextCursor=null;

        if(!postList.isEmpty()){
            Post lastPost=postList.get(postList.size()-1);
             nextCursor=postStatus== Post.PostStatus.PUBLISHED?lastPost.getPublishedAt():lastPost.getCreatedAt();
        }

        return new PostPreviewResponse(postPreviewRepresentationList,hasMore,nextCursor);
    }

}
