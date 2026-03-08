package com.example.SocialMediaApp.SocialGraph.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
@NoArgsConstructor
@Table(indexes ={
        @Index(name ="follower_following",columnList = "follower_id,following_id,status",unique = true),
        @Index(name="follower",columnList = "follower_id,status"),
        @Index(name="following",columnList = "following_id,status"),
} )
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    private Instant createddate;

    @Column(name ="accepteddate")
    private Instant followDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "follower_id",insertable = false,updatable = false)
    private String follower_id;

    @Column(name = "following_id",insertable = false,updatable = false)
    private String following_id;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="follower_id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="following_id")
    private User following;

    public Follow(String followerId, String followingId){
       this.follower=new User(followerId);
       this.following=new User(followingId);
    }

    public Follow(String follower, String following, Status status){
        this(follower,following);
        this.status=status;
    }

    public enum Status{PENDING,ACCEPTED}
}
