package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.application.PostLifecycleService;
import com.example.SocialMediaApp.Content.application.PostSchedulingService;
import com.example.SocialMediaApp.Content.application.PostUpdateService;
import com.example.SocialMediaApp.Content.application.PostVisibilityService;
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
    private final PostUpdateService postUpdateService;
    private final PostVisibilityService postVisibilityService;
    private final PostSchedulingService postSchedulingService;
    
    @PostMapping
    public ResponseEntity<PostRepresentation> createPost(@RequestBody @Valid PostCreationRequest postCreation) throws SchedulerException {
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


    @PatchMapping("/{postId}/unSchedule")
    public ResponseEntity<Void> unSchedulePost(@PathVariable String postId) throws SchedulerException {
        postSchedulingService.unSchedulePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/visibility")
    public ResponseEntity<PostVisibilityToggleResponse> toggleVisibility(@PathVariable String postId) {
        return ResponseEntity.ok(postVisibilityService.togglePostVisibility(postId));
    }

    @GetMapping("/{postId}/update")
    public ResponseEntity<PostUpdateResponse> getPostToUpdate(@PathVariable String postId){
        return ResponseEntity.ok(postUpdateService.getPostToUpdate(postId));
    }

    @PatchMapping
    public ResponseEntity<Void> updatePost(@RequestBody @Valid PostUpdateRequest request){
        postUpdateService.updatePost(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/publish")
    public ResponseEntity<Void> publishPost(){

        return ResponseEntity.noContent().build();
    }


}
