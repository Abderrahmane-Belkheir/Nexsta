package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.api.dto.chatMemberDTO;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMemberRepo extends JpaRepository<ChatMember,UUID> {
    @Query( "SELECT ChatMember cm FROM ChatMember WHERE cm.id.chatId IN (:chatIds) AND cm.id.userId != :userId")
    List<ChatMember> findOtherChatMembers(@Param("chatIds") List<String> chatIds, @Param("userId") String userId);
    boolean existsByUserIdAndChatId(String userId,String chatId);
    Optional<ChatMember> findByChatIdAndUserIdNot(String chat_uuid,String user_id);



    @Transactional
    @Modifying
    @Query("UPDATE ChatMember cm SET cm.unReadCount=cm.unReadCount+1 WHERE cm.id.chatId=:chatId AND cm.id.userId NOT IN (:usersId) ")
    void incrementUnReadCount(@Param("chatId") String chatId,@Param("usersId") List<String> usersId);

    @Query("""
    SELECT cm FROM ChatMember cm
    WHERE cm.id.userId = :userId
    AND cm.id.chatId IN :chatIds
    """)
    List<ChatMember> findCurrentUserMembers(
            @Param("userId") String userId,
            @Param("chatIds") List<String> chatIds
    );


}
