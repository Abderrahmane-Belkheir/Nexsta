package com.Nexsta.Content.domain;

import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"story_id", "user_id"}),indexes ={
        @Index(name = "story_user",columnList = "story_id,user_id",unique = true)
})
public class StoryView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant viewedAt;

    @ManyToOne
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public StoryView(String storyId,String userId){
        this.story=new Story(storyId);
        this.user=new User(userId);
    }

}
