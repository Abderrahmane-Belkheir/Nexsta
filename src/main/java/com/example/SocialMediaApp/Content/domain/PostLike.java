package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@Table(indexes ={
        @Index(name ="user_like",columnList = "post_id,user_id",unique = true)
})
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant likedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public PostLike(String userId,String postId){
        this.user=new User(userId);
        this.post=new Post(postId);
    }
}
