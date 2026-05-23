package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.domain.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatRepo extends JpaRepository<Chat,String> {
    @Query("""
    SELECT c.id FROM Chat c
    JOIN c.members cm
    WHERE cm.id.userId = :userId
    AND (:cursor IS NULL OR c.lastMessageAt < :cursor)
    ORDER BY c.lastMessageAt DESC
    """)
    List<String> findUserChatIds(
            @Param("userId") String userId,
            @Param("cursor") Instant cursor,
            Pageable pageable
    );

    // step 2 — fetch full chats with members by ids
    @Query("""
    SELECT DISTINCT c FROM Chat c
    JOIN FETCH c.members
    WHERE c.id IN :chatIds
    ORDER BY c.lastMessageAt DESC
    """)
    List<Chat> findChatsByIds(@Param("chatIds") List<String> chatIds);

    @Query("""
           SELECT c FROM Chat c
           JOIN c.members cm
           WHERE cm.id.userId IN (:user1Id, :user2Id)
           GROUP BY c
           HAVING COUNT(DISTINCT cm.userId) = 2
     
     """)
    Optional<Chat> findChatBetween(@Param("user1Id") String user1Id,@Param("user2Id") String user2Id);

    @Query("SELECT c FROM Chat c JOIN c.members cm WHERE c.id=:chatId AND cm.id.userId=:userId ")
    Optional<Chat> findChatById(@Param("chatId") String chatId,@Param("userId") String userId);

}
