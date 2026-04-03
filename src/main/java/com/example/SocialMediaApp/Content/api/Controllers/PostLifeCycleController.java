package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.application.PostLifecycleService;
import com.example.SocialMediaApp.Content.domain.Post;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
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
    public ResponseEntity<DeletePostResponse>  deletePost(@PathVariable  String postId) throws SchedulerException {
       return ResponseEntity.ok(postLifecycleService.deletePost(postId));
    }

    @PutMapping("/{postId}/restore")
    public ResponseEntity<PostRepresentation> restorePost(@PathVariable String postId){
        return ResponseEntity.ok(postLifecycleService.restorePost(postId));
    }

    @PatchMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(@RequestBody @Valid PostPublish postPublish) throws SchedulerException {
        postLifecycleService.publishPost(postPublish);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/unSchedule")
    public ResponseEntity<Void> unSchedulePost(@PathVariable String postId) throws SchedulerException {
        postLifecycleService.unSchedulePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<PostVisibilityToggleResponse> toggleVisibility(@PathVariable String postId) {
        return ResponseEntity.ok(postLifecycleService.togglePostVisibility(postId));
    }


}
