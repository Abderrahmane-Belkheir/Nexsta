package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.domain.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepo extends JpaRepository<Chat,String> {
    @Query(""" 
    SELECT c
    FROM Chat c
    JOIN c.members cm
    WHERE cm.user.id = :userId
    ORDER BY c.lastMessageAt DESC
""")
    Page<Chat> findByUserId(String userId, Pageable pageable);

    @Query("""
          SELECT c FROM Chat c
          JOIN c.members cm
           WHERE cm.userId IN (:user1Id, :user2Id)
           GROUP BY c
           HAVING COUNT(DISTINCT cm.userId) = 2
     
     """)
    Optional<Chat> findChatBetween(@Param("user1Id") String user1Id,@Param("user2Id") String user2Id);
}
