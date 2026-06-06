package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.domain.Message;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends MongoRepository<Message,String> {



    Optional<Message> findByIdAndSenderIdAndDeletedFalse(String id, String senderId);

    List<Message> findByIdIn(List<String> lastMessageIds);

    @Query("{ 'chatId': ?0, $nor: [ { 'deleted': true, 'senderId': ?1 } ] }")
    List<Message> findLatestVisibleMessages(String chatId, String viewerId, Pageable pageable);

    @Query("{ 'chatId': ?0,'_id': { $lt: ?2 }, $nor: [ { 'deleted': true, 'senderId': ?1 } ] }")
    List<Message> findVisibleMessagesBeforeId(String chatId, String viewerId, ObjectId beforeId, Pageable pageable);

    Optional<Message> findFirstByChatIdAndIdLessThanOrderByIdDesc(String chatId, ObjectId messageId);
}
