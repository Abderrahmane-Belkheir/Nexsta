package com.Nexsta.Messaging.persistence;

import com.Nexsta.Messaging.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepo extends MongoRepository<Message,String> {



    List<Message> findByIdIn(List<String> lastMessageIds);

    List<Message> findByChatIdOrderByIdDesc(String chatId, Pageable pageable);

    List<Message> findByChatIdAndIdLessThanOrderByIdDesc(
            String chatId,
            String beforeId,
            Pageable pageable
    );

}
