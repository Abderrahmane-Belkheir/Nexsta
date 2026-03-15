package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.application.PostQueryService;
import com.example.SocialMediaApp.Content.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/post")
public class PostQueryController {

    private final PostQueryService postQueryService;

    @GetMapping("/me")
    public ResponseEntity<Page<PostRepresentation>> getMyPosts(@RequestParam(defaultValue = "PUBLISHED") Post.PostStatus status,@RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(postQueryService.getMyPosts(status,page));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<PostRepresentation>> getUserPosts(@PathVariable String userId, @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(postQueryService.getUserPosts(userId,page));
    }



}

