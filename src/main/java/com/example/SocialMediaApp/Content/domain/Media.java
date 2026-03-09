package com.example.SocialMediaApp.Content.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Builder
public class Media {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String filepath;

    private int displayOrder;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "post_id",insertable = false,updatable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    @Column(name = "story_id",insertable = false,updatable = false)
    private String storyId;



    public enum MediaType {
        IMAGE,VIDEO
    }

}
