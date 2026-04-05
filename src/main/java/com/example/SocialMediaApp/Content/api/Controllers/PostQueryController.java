package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.PostPreviewRepresentation;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.application.FullPostQueryService;
import com.example.SocialMediaApp.Content.application.PostPreviewQueryService;
import com.example.SocialMediaApp.Content.domain.FetchDirection;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/post")
public class PostQueryController {

    private final FullPostQueryService fullPostQueryService;
    private final PostPreviewQueryService postPreviewQueryService;

    @GetMapping("/me")
    public ResponseEntity<Page<PostPreviewRepresentation>> getMyPostsPreview(@RequestParam(defaultValue = "PUBLISHED") Post.PostStatus status,@RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(postPreviewQueryService.getMyPostsPreview(status,page));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<PostPreviewRepresentation>> getUserPostsPreview(@PathVariable String userId, @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(postPreviewQueryService.getUserPostsPreview(userId,page));
    }

    @GetMapping("/{postId}/neighbors")
    public ResponseEntity<Page<PostRepresentation>> getUserPostsRepresentation(@PathVariable String postId,@RequestParam(defaultValue = "MIXED") FetchDirection direction){
        return ResponseEntity.ok(fullPostQueryService.getPostNeighbors(postId,direction));
    }






}

