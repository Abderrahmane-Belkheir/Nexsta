package com.Nexsta.Content.domain;

import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="comment_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="story_id")
    @OnDelete(action=OnDeleteAction.CASCADE)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;




    public Like(String userId){
        this.user=new User(userId);
    }

    public Like(String userId,String targetId,LikeType type){
        this(userId);
        this.type=type;
        defineTarget(targetId);
    }

    private void defineTarget(String targetId){
        if(type==LikeType.STORY){
            this.story=new Story(targetId);
        }else if(type==LikeType.COMMENT){
            this.comment=new Comment(targetId);
        }
    }

}
