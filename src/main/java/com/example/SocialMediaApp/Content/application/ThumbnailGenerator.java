package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.domain.Media;
import com.example.SocialMediaApp.Content.domain.PostPreview;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThumbnailGenerator {

    public PostPreview generatePostThumbnail(Media media){
        return new PostPreview(media.getMediaType(),media.getId());
    }

}
