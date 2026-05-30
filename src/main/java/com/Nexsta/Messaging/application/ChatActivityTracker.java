package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.TypingEvent;
import com.Nexsta.Messaging.api.dto.TypingPayload;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.persistence.ChatRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChatActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;
    private final RealTimeDeliveringService realTimeDeliveringService;
    private final ChatRepo chatRepo;
    private static final String CHAT_ACTIVE_KEY = "chat:active:";
    private static final String INBOX_ACTIVE_KEY = "inbox:active";
    private static final Duration TTL = Duration.ofSeconds(10);


    public void userEnteredChat(String userId, String chatId) {
        redisTemplate.opsForValue().set(
                CHAT_ACTIVE_KEY + chatId + ":" + userId,
                "1",
                TTL
        );
    }

    public void userLeftChat(String userId, String chatId) {
        redisTemplate.delete(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public boolean isUserActiveInChat(String userId, String chatId) {
        return redisTemplate.hasKey(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public void userOpenedInbox(String userId) {
        redisTemplate.opsForValue().set(
                INBOX_ACTIVE_KEY + userId,
                "1",
                TTL
        );
    }

    public boolean isUserActiveInInbox(String userId) {
        return redisTemplate.hasKey(INBOX_ACTIVE_KEY + userId);
    }

    @Transactional(readOnly = true)
    public void deliverTypingEvent(String chatId, String userId, TypingPayload typingPayload){
    Chat chat=chatRepo.findChatById(chatId,userId).orElseThrow(()-> new ContentNotAvailableException("Chat Not Found"));

    if(chat.getType()!= Chat.ChatType.DIRECT) {
        return;
    }

        List<String> activeUsersInChat=new ArrayList<>();

     for(ChatMember chatMember:chat.getMembers()){

        String memberId=chatMember.getId().getUserId();

         if(memberId.equals(userId)) continue;

         if(isUserActiveInChat(memberId,chatId)){
             activeUsersInChat.add(memberId);
         }
     }

     TypingEvent typingEvent=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
             TypingEvent.start(chatId,userId):TypingEvent.stop(chatId,userId);

     realTimeDeliveringService.deliverTypingEvent(activeUsersInChat, typingEvent);
    }

}






