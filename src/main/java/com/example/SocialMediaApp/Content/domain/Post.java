package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;




import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}),indexes ={
        @Index(name ="user_post",columnList = "user_id")
})
public class Post {

    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    private Instant scheduledAt;

    private Instant publishedAt;

    private Instant unPublishedAt;

    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus postStatus=PostStatus.DRAFT;

    private String caption;

    @Embedded
    private PostPreview postPreview;

    @Builder.Default
    private Long likeCount=0L;

    @Builder.Default
    private Long commentCount=0L;

    @Column(name = "restored", nullable = false, columnDefinition = "boolean default false")
    private boolean restored = false;

    @Enumerated(EnumType.STRING)
    private PostStatus preDeletionStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private PostSettings postSettings;

    private String postFolderPath;


    @OneToMany(mappedBy = "post",fetch = FetchType.LAZY,orphanRemoval = true,cascade =CascadeType.ALL)
    @Builder.Default
    @OrderBy("displayOrder")
    @BatchSize(size = 30)
    private List<Media> mediaList=new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name="user_id",insertable = false,updatable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Location location;

    public enum PostStatus{
        PUBLISHED,UNPUBLISHED,DRAFT,DELETED,SCHEDULED
    }

    public Post(String id){
        this.id=id;
    }

}
