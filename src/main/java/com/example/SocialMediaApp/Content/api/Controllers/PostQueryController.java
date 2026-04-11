package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.*;
import com.example.SocialMediaApp.Content.application.FullPostQueryService;
import com.example.SocialMediaApp.Content.application.PostPreviewQueryService;
import com.example.SocialMediaApp.Content.domain.FetchDirection;
import com.example.SocialMediaApp.Content.domain.Post;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/post")
public class PostQueryController {

    private final FullPostQueryService fullPostQueryService;
    private final PostPreviewQueryService postPreviewQueryService;

    @GetMapping("/me")
    public ResponseEntity<PostPreviewResponse> getMyPostsPreview(@ModelAttribute @Valid PostPreviewRequest request){
        return ResponseEntity.ok(postPreviewQueryService.getMyPostsPreview(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<PostPreviewResponse> getUserPostsPreview(@PathVariable String userId,@RequestBody @Valid PostPreviewRequest request){
        return ResponseEntity.ok(postPreviewQueryService.getUserPostsPreview(userId,request));
    }

    @GetMapping("/{cursor}/neighbors")
    public ResponseEntity<PostRepresentationResponse> getUserPostsRepresentation(@PathVariable String cursor, @RequestParam(defaultValue = "MIXED") FetchDirection direction){
        return ResponseEntity.ok(fullPostQueryService.getPostNeighbors(cursor,direction));
    }






}

