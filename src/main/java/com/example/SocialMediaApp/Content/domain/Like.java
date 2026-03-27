package com.example.SocialMediaApp.Content.domain;

import com.example.SocialMediaApp.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "likes")
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private LikeType type;

    // this could be (story,comment)
    private String targetId;


    @ManyToOne
    @JoinColumn(name= "user_id")
    private User user;




    public Like(String userId){
        this.user=new User(userId);
    }

    public Like(String userId,String targetId,LikeType type){
        this(userId);
        this.type=type;
        this.targetId=targetId;
    }

}
