package com.Nexsta.Profile.domain;



import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes={
        @Index(name="user_profile",columnList = "user_id")
        })
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String bio;

    private String username;

    private String avatarPath;

    @Embedded
    private ProfileSettings profileSettings=new ProfileSettings();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(name ="user_id",insertable = false,updatable = false)
    private String userId;

    public Profile(String bio , String username){
    this.bio=bio;
    this.username=username;
}

    public Profile(String username){
    this(null,username);
}

}
