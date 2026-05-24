package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.domain.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface ChatMemberRepo extends JpaRepository<ChatMember,UUID> {

    @Transactional
    @Modifying
    @Query("UPDATE ChatMember cm SET cm.unReadCount=0,cm.lastReadMessageId=:lastReadMessageId WHERE cm.id.chatId=:chatId AND cm.id.userId = :userId ")
    void resetCountAndUpdateLastReadMessage(@Param("chatId") String chatId,@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMember cm SET cm.unReadCount=cm.unReadCount+1 WHERE cm.id.chatId=:chatId AND cm.id.userId NOT IN (:usersId) ")
    void incrementUnReadCount(@Param("chatId") String chatId,@Param("usersId") List<String> usersId);

    @Query(value = "SELECT user_id FROM Chat_Member WHERE chat_id IN :chatsId AND unReadCount=0 ",nativeQuery = true)
    List<UserId> findMembersWhoSeenLastMessage(@Param("chatsId") List<String> chatsId);

    @Query(value = "SELECT chat_id,unReadCount FROM Chat_Member WHERE user_id=:userId AND chat_id IN :chatsId  ",nativeQuery = true)
    List<ChatUnread> findUnreadCountsForUser(@Param("userId") String userId,@Param("chatsId") List<String> chatsId);

     interface UserId {
        String getUserId();
    }

    interface ChatUnread{
         String getChatId();
         int getUnread();
    }
}
