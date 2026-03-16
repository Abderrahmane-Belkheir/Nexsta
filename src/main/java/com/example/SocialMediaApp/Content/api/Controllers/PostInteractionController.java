package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.CommentCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.application.CommentQueryService;
import com.example.SocialMediaApp.Content.application.PostInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/post")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<LikeResponse> likePost(@PathVariable String postId){
        return ResponseEntity.ok(postInteractionService.addPostLike(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentRepresentation> commentPost(@PathVariable String postId, @RequestBody @Valid CommentCreationRequest commentRequest){
        return ResponseEntity.ok(postInteractionService.addPostComment(postId,commentRequest));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String postId,@PathVariable String commentId){
        postInteractionService.removePostComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentRepresentation>> getPostComment(@PathVariable String postId, @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(commentQueryService.getPostComments(postId,page));
    }

}
