package com.Nexsta.Messaging.domain;

import com.Nexsta.User.domain.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(indexes={
        @Index(name="chat_userid",columnList = "user_id")
})
public class ChatMember {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "chat_id",updatable = false,insertable = false)
    private String chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @Column(name="user_id",updatable = false,insertable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int unreadCount;

    private String lastreadMessageId;

    public ChatMember(Chat chat,String userId){
        this.chat=chat;
        this.user=new User(userId);
        }

        public void incrementUnreadCount(){
        if(unreadCount<4){
            unreadCount+=1;
        }
        }
}
