package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.CommentCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentRepresentation;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.application.CommentInteractionService;
import com.example.SocialMediaApp.Content.application.CommentQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1//content/comments")
public class CommentController {


    private final CommentInteractionService commentInteractionService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<LikeResponse> likeComment(@PathVariable String commentId){
       return ResponseEntity.ok(commentInteractionService.addCommentLike(commentId));
    }


    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentRepresentation>  replyComment(@PathVariable String commentId, @RequestBody @Valid CommentCreationRequest commentRequest){
         return ResponseEntity.ok(commentInteractionService.addCommentReply(commentId,commentRequest));
    }


    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Page<CommentRepresentation>> getCommentReplies(@PathVariable String commentId,@RequestParam(defaultValue ="0") int page){
        return ResponseEntity.ok(commentQueryService.getCommentReplies(commentId,page));
    }



}
