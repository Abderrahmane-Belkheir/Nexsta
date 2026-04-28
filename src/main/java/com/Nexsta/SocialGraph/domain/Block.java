package com.Nexsta.SocialGraph.domain;

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
@Table(indexes = {
        @Index(name="idx_block",columnList = "blocked_id,blocker_id")
})
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreatedDate
    private Instant blockedat;

    @ManyToOne
    @JoinColumn(name = "blocker_id")
    private User blocker;

    @ManyToOne
    @JoinColumn(name="blocked_id")
    private User blocked;

    public Block(String blockerId, String blockedId){
        this.blocked=new User(blockedId);
        this.blocker=new User(blockerId);
    }
}
