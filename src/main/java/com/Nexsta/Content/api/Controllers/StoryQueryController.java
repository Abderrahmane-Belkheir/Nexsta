package com.Nexsta.Content.api.Controllers;

import com.Nexsta.Content.api.dto.StoryRepresentation;
import com.Nexsta.Content.application.StoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/content/story")
public class StoryQueryController {

    private final StoryQueryService storyQueryService;


    @GetMapping("/me")
    public ResponseEntity<Page<StoryRepresentation>> getMyStories(){
        return ResponseEntity.ok(storyQueryService.getMyStories());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Page<StoryRepresentation>> getUserPosts(@PathVariable String userId, @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(storyQueryService.getUserStories(userId));
    }


}
