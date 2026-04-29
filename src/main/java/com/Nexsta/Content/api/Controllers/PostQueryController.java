package com.Nexsta.Content.api.Controllers;

import com.Nexsta.Content.api.dto.PostPreviewRequest;
import com.Nexsta.Content.api.dto.PostPreviewResponse;
import com.Nexsta.Content.api.dto.PostRepresentationResponse;
import com.Nexsta.Content.api.dto.*;
import com.Nexsta.Content.application.FullPostQueryService;
import com.Nexsta.Content.application.PostPreviewQueryService;
import com.Nexsta.Content.domain.FetchDirection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PostPreviewResponse> getUserPostsPreview(@PathVariable String userId,@ModelAttribute @Valid PostPreviewRequest request){
        return ResponseEntity.ok(postPreviewQueryService.getUserPostsPreview(userId,request));
    }

    @GetMapping("/{cursor}/neighbors")
    public ResponseEntity<PostRepresentationResponse> getUserPostsRepresentation(@PathVariable String cursor, @RequestParam(defaultValue = "MIXED") FetchDirection direction){
        return ResponseEntity.ok(fullPostQueryService.getPostNeighbors(cursor,direction));
    }






}

