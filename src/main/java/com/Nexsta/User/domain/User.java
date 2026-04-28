package com.Nexsta.User.domain;

import com.Nexsta.Notification.domain.NotificationsSettings;
import com.Nexsta.Profile.domain.Profile;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String userName;

    @Email
    @Column(unique = true)
    private String email;

    private LocalDate birthDay;

    private Long followerCount=0L;

    private Long followingCount=0L;

    private Long postCount=0L;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private NotificationsSettings notificationsSettings;

    public User(String userName, String firstName, String lastName, String email){
        this.firstName= firstName;
        this.lastName= lastName;
        this.email=email;
        this.userName= userName;
    }

    public User(String uuid){
     this.id=uuid;
    }

    public User(String userName, String uuid){
        this.userName= userName;
        this.id=uuid;
    }

}
