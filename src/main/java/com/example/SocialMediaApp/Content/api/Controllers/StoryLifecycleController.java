package com.example.SocialMediaApp.Content.api.Controllers;

import com.example.SocialMediaApp.Content.api.dto.StoryCreationRequest;
import com.example.SocialMediaApp.Content.api.dto.StoryRepresentation;
import com.example.SocialMediaApp.Content.application.StoryLifecycleService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/story")
public class StoryLifecycleController {

    private final StoryLifecycleService storyLifecycleService;


    @GetMapping("/new")
    public void redirectPost(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) throws IOException {
        String token = jwt.getTokenValue();
        String redirectUrl = UriComponentsBuilder.fromUriString("/create-story.html")
                .queryParam("token", token)
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    @PostMapping
    @Hidden
    public ResponseEntity<StoryRepresentation> createStory(@RequestBody @Valid StoryCreationRequest storyCreation){
        return ResponseEntity.ok(storyLifecycleService.createStory(storyCreation));
    }

    @PatchMapping("/{storyId}/publish")
    public ResponseEntity<Void> publishStory(@PathVariable String storyId){
        storyLifecycleService.publishStory(storyId);
        return ResponseEntity.noContent().build();
    }


}
