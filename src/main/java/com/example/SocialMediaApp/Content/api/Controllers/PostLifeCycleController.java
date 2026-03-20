package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.PostCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.PostRepresentation;
import com.example.SocialMediaApp.Content.application.PostLifecycleService;
import com.example.SocialMediaApp.Content.domain.Post;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/post")
@Validated
public class PostLifeCycleController {

    private final PostLifecycleService postLifecycleService;

    @GetMapping("/new")
    public void redirectPost(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) throws IOException {
        String token = jwt.getTokenValue();
        String redirectUrl = UriComponentsBuilder.fromUriString("/create-post.html")
                .queryParam("token", token)
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping
    public ResponseEntity<PostRepresentation> createPost(@RequestBody @Valid PostCreationRequest postCreation){
        return ResponseEntity.ok(postLifecycleService.createPost(postCreation));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void>  deletePost(@PathVariable  String postId){
        postLifecycleService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{postId}/restore")
    public ResponseEntity<PostRepresentation> restorePost(@PathVariable String postId){
        return ResponseEntity.ok(postLifecycleService.restorePost(postId));
    }

    @PatchMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(@PathVariable String postId) {
        postLifecycleService.publishPost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<Void> toggleVisibility(@PathVariable String postId,
                                                 @RequestParam Post.PostStatus status) {
        postLifecycleService.togglePostVisibility(postId, status);
        return ResponseEntity.noContent().build();
    }


}
