package com.example.SocialMediaApp.User.domain;

import com.example.SocialMediaApp.Notification.domain.NotificationsSettings;
import com.example.SocialMediaApp.Profile.domain.Profile;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="USERS")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Data
public class User {

    @Id
    private String id;

    @CreatedDate
    private Instant createddate;

    @LastModifiedDate
    private Instant lastmodifieddate;

    private String firstname;

    private String lastname;

    @Column(unique = true)
    private String username;

    @Email
    @Column(unique = true)
    private String email;

    private LocalDate birthday;

    private Long followerCount=0L;

    private Long followingCount=0L;

    private Long postCount=0L;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private NotificationsSettings notificationsSettings;

    public User(String username, String firstname, String lastname, String email){
        this.firstname=firstname;
        this.lastname=lastname;
        this.email=email;
        this.username=username;
    }

    public User(String uuid){
     this.id=uuid;
    }

    public User(String username, String uuid){
        this.username=username;
        this.id=uuid;
    }

}
