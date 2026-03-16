package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(indexes ={
        @Index(name ="post_comment",columnList = "post_id")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant createdAt;

    private Long likeCount=0L;

    private Long replyCount=0L;

    @Column(updatable = false)
    private String postOwnerId;

    @Size(max = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "post_id",updatable = false,insertable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private User user;

    @Column(name = "user_id",updatable = false,insertable = false)
    private String userId;

    @OneToMany(mappedBy = "parentComment",fetch = FetchType.LAZY,orphanRemoval = true)
    private List<Comment> replies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parentComment;

    public Comment(Comment parentComment,String content,String userId,String postId,String postOwnerId){
        this.parentComment=parentComment;
        this.content=content;
        this.user=new User(userId);
        this.post=new Post(postId);
        this.postOwnerId=postOwnerId;
    }
    public Comment(String id){
        this.id=id;
    }

    public enum CommentType{REPLY,COMMENT};

}
