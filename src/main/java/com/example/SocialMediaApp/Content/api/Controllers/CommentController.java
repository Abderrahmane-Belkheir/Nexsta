package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentResponse;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.application.CommentInteractionService;
import com.example.SocialMediaApp.Content.application.CommentQueryService;
import com.example.SocialMediaApp.Content.application.PostInteractionService;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {


    private final CommentInteractionService commentInteractionService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<LikeResponse> likeComment(@PathVariable String commentId){
       return ResponseEntity.ok(commentInteractionService.addCommentLike(commentId));
    }


    @PostMapping("/{commentId}/replies")
    public ResponseEntity<Void>  replyComment(@PathVariable String commentId, @RequestBody @Valid CommentRequest commentRequest){
        commentInteractionService.addCommentReply(commentId,commentRequest);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<?>> getCommentReplies(@PathVariable String commentId,@RequestParam(defaultValue ="0") int page){
        return ResponseEntity.ok(commentQueryService.getCommentReplies(commentId,page));
    }



}
