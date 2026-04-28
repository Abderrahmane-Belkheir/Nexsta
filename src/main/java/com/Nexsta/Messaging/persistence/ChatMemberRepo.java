package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.api.dto.chatMemberDTO;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMemberRepo extends JpaRepository<ChatMember,UUID> {
    @Query(value = "SELECT user_id, chat_id FROM chatMember WHERE chat_id IN (:chatIds) AND user_id != :userId", nativeQuery = true)
    List<chatMemberDTO> findOtherChatMembers(@Param("chatIds") List<String> chatIds, @Param("userId") String userId);
    List<ChatMember> findByUserAndChatIdIn(User user,List<String> chatIds);
    boolean existsByUserIdAndChatId(String userId,String chatId);
    Optional<ChatMember> findByChatIdAndUserIdNot(String chat_uuid,String user_id);
    Optional<ChatMember>  findByChatIdAndUserId(String chat_uuid,String user_id);
    @Modifying
    @Query("update ChatMember cm set cm.unreadCount=0,cm.lastreadMessageId=:lastmessageId where cm.userId=:userId and cm.chatId=:chatId")
    void updateUserChat(@Param("lastmessageId") String lastmessageIdn,@Param("userId") String userId,@Param("chatId") String chatId);


}
