package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.persistence.ChatRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TypingEventRelay {

    private final ChatRepo chatRepo;
    private final ChatMemberCache chatMemberCache;
    private final InstanceRouter instanceRouter;

    @Transactional(readOnly = true)
    public void deliverTypingEvent(String chatId, String userId, TypingPayload typingPayload){

        List<String> memberIds=chatMemberCache.get(chatId).
                orElseGet(()->{
                    List<String> membersId = chatRepo.findChatById(chatId,userId).
                            orElseThrow(()->new ContentNotAvailableException("Chat Not Found")).getMembers().
                            stream().map(chatMember -> chatMember.getId().getUserId()).toList();
                  chatMemberCache.put(chatId,membersId);
                  return membersId;
                });

        if(!memberIds.contains(userId)) {
            throw new ContentNotAvailableException("Chat Not Found");

        }

        memberIds.remove(userId);

        ActivityMaps activityMaps=instanceRouter.buildActivityMaps(memberIds,chatId);
        Map<String, List<String>> inChatMap  = activityMaps.getInChatMap();
        Map<String, List<String>> inInboxMap = activityMaps.getInInboxMap();
        if(!inChatMap.isEmpty()){
            TypingEvent typingEvent=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
                    TypingEvent.start(chatId,userId):TypingEvent.stop(chatId,userId);
            instanceRouter.routeBatch(inChatMap, ids->new TypingDelivery(ids,typingEvent));
        }
        if(!inInboxMap.isEmpty()){
            InboxEvent inboxEvent=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
                    InboxEvent.typingMessageStarted(chatId,userId):InboxEvent.typingMessageStopped(chatId,userId);
            instanceRouter.routeBatch(inInboxMap, ids->new InboxDelivery(ids,inboxEvent));
        }


    }
}
