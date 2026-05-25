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
        @Index(name="chat_userid",columnList = "user_id"),
        @Index(name = "chat_unread",columnList = "chat_id,unReadCount")
})
public class ChatMember {

    @EmbeddedId
    private ChatMemberId id=new ChatMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    @MapsId("chatId")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    private User user;

    private int unReadCount;

    private String lastReadMessageId;

    @Transient
    private String avatarUrl;

    public ChatMember(Chat chat,String userId){
        this.chat=chat;
        this.user=new User(userId);
        }


}
