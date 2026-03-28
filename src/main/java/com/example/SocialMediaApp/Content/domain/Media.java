package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.Storage.StorageTransfer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(name = "post_id",insertable = false,updatable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    @Column(name = "story_id",insertable = false,updatable = false)
    private String storyId;

    public void transformFilePath(StorageTransfer storageTransfer){
        this.filepath=filepath.replace(storageTransfer.getSource().getDirName(),storageTransfer.getDestination().getDirName());
    }


    public enum MediaType {
        IMAGE,VIDEO
    }

}
