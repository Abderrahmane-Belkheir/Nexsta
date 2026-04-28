package com.Nexsta.Content.domain;

import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@AllArgsConstructor
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

    private long likeCount=0L;

    private Long replyCount;

    @Column(updatable = false)
    private String postOwnerId;

    @Size(max = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(name = "post_id",updatable = false,insertable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "user_id",updatable = false,insertable = false)
    private String userId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parentComment;

    public Comment(Comment parentComment,String content,String userId,String postId,String postOwnerId){
        this.parentComment=parentComment;
        // replies don't have reply counts, null signals mapper to exclude this field
        this.replyCount = parentComment == null ? 0L : null;
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
