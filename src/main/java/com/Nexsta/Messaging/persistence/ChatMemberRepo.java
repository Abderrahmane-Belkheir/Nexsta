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

    @Query(value = "SELECT c.id As chatId ,cm.user_id As userId FROM Chat c JOIN Chat_Member cm ON c.id=cm.chat_id WHERE c.id IN :chatsId AND cm.user_id!=:userId ",nativeQuery = true)
    List<ChatMember> findOtherChatMember(@Param("chatsId") List<String> chatsId,@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMember cm SET cm.unReadCount=0,cm.lastReadMessageId=:lastReadMessageId WHERE cm.id.chatId=:chatId AND cm.id.userId = :userId ")
    void resetCountAndUpdateLastReadMessage(@Param("chatId") String chatId,@Param("userId") String userId,@Param("lastReadMessageId") String lastReadMessageId);

    @Transactional
    @Modifying
    @Query("UPDATE ChatMember cm SET cm.unReadCount=cm.unReadCount+1 WHERE cm.id.chatId=:chatId AND cm.id.userId!=:userId AND cm.id.userId NOT IN (:usersId) ")
    void incrementUnReadCount(@Param("chatId") String chatId,@Param("userId") String userId,@Param("usersId") List<String> usersId);

    @Query(value = "SELECT user_id As userId,chat_id As chatId FROM Chat_Member WHERE chat_id IN :chatsId AND user_id!=:userId AND un_read_count=0 ",nativeQuery = true)
    List<ChatMember> findMembersWhoSeenLastMessage(@Param("chatsId") List<String> chatsId,@Param("userId") String userId);

    @Query(value = "SELECT chat_id As chatId ,un_read_count As unRead FROM Chat_Member WHERE user_id=:userId AND chat_id IN :chatsId ",nativeQuery = true)
    List<ChatUnread> findUnreadCountsForUser(@Param("userId") String userId,@Param("chatsId") List<String> chatsId);


    interface ChatUnread{
         String getChatId();
         Integer getUnRead();
    }
    interface ChatMember{
         String getUserId();
         String getChatId();
    }
}
