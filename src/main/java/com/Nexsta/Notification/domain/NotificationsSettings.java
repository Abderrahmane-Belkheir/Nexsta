package com.Nexsta.Notification.domain;

import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(indexes = {@Index(name="userNotifications",columnList = "user_id")})
public class NotificationsSettings {
    @Id
    @GeneratedValue
    private UUID id;
   // @Column(name="likes")
    //private Boolean Onlikes;
    //@Column(name = "comments")
    //private Boolean Oncomments;
    //@Column(name="commentreplies")
    //private Boolean Oncommentreplies;

    @Column(name="follow")
    private Boolean Onfollow=true;

    @Column(name="followingrequests")
    private Boolean onfollowingrequestAccepted=true;

    @Column(name="followingrequests_rejected")
    private Boolean onfollowingrequestRejected=true;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;
}