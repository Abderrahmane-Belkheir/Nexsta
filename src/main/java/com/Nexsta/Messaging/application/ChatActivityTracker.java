package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.TypingDelivery;
import com.Nexsta.Messaging.api.dto.TypingEvent;
import com.Nexsta.Messaging.api.dto.TypingPayload;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Shared.ServerInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChatActivityTracker {

    private final RedisTemplate<String,String> redisTemplate;
    private final RedisTemplate<String,Object> objectRedisTemplate;
    private final RealTimeDeliveringService realTimeDeliveringService;
    private final ChatRepo chatRepo;
    private static final String CHAT_ACTIVE_KEY = "chat:active:";
    private static final String INBOX_ACTIVE_KEY = "inbox:active:";
    private static final Duration TTL = Duration.ofSeconds(10);
    private final ServerInstance serverInstance;

    public void userEnteredChat(String userId, String chatId) {
        redisTemplate.opsForValue().set(
                CHAT_ACTIVE_KEY + chatId + ":" + userId,
                serverInstance.getInstanceId(),
                TTL
        );
    }

    public void userLeftChat(String userId, String chatId) {
        redisTemplate.delete(CHAT_ACTIVE_KEY + chatId + ":" + userId);
    }

    public Optional<String> isUserActiveInChat(String userId, String chatId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CHAT_ACTIVE_KEY + chatId + ":" + userId));
    }

    public void userOpenedInbox(String userId) {
        log.info("user {} opened chat {} ",userId,serverInstance.getInstanceId());
        redisTemplate.opsForValue().set(
                INBOX_ACTIVE_KEY + userId,
                serverInstance.getInstanceId(),
                TTL
        );
    }

    public Optional<String> isUserActiveInInbox(String userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(INBOX_ACTIVE_KEY + userId));
    }

    @Transactional(readOnly = true)
    public void deliverTypingEvent(String chatId, String userId, TypingPayload typingPayload){
    Chat chat=chatRepo.findChatById(chatId,userId).orElseThrow(()-> new ContentNotAvailableException("Chat Not Found"));

    if(chat.getType()!= Chat.ChatType.DIRECT) {
        return;
    }

        List<String> activeUsersInChat=new ArrayList<>();
    Map<String,List<String>> activeUsersInChatMap=new HashMap<>();
        List<ChatMember> members=chat.getMembers().stream().filter(chatMember -> !chatMember.getId().getUserId().equals(userId)).toList();
        for(ChatMember member:members){
            String memberId=member.getId().getUserId();
            Optional<String> memberInChatInstanceId =isUserActiveInChat(memberId,chat.getId());
            memberInChatInstanceId.ifPresent(s -> activeUsersInChatMap.computeIfAbsent(s, k -> new CopyOnWriteArrayList<>()).add(memberId));
            }

     if(activeUsersInChatMap.isEmpty()){
         return;
     }

     TypingEvent event=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
             TypingEvent.start(chatId,userId):TypingEvent.stop(chatId,userId);
        for(Map.Entry<String,List<String>> entry:activeUsersInChatMap.entrySet()){
            String membersInstanceId=entry.getKey();
            TypingDelivery typingDelivery=new TypingDelivery(new ArrayList<>(entry.getValue()),event);
            if(membersInstanceId.equals(serverInstance.getInstanceId())){
                realTimeDeliveringService.deliverTypingEvent(typingDelivery);
            }else {
                objectRedisTemplate.convertAndSend(membersInstanceId,typingDelivery);
            }
    }

}

}




