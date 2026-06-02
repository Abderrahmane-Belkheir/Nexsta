package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.persistence.ChatRepo;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class TypingEventRelay {

    private final ChatRepo chatRepo;
    private final ChatActivityTracker chatActivityTracker;
    private final InstanceRouter instanceRouter;

    @Transactional(readOnly = true)
    public void deliverTypingEvent(String chatId, String userId, TypingPayload typingPayload){
        Chat chat=chatRepo.findChatById(chatId,userId).orElseThrow(()-> new ContentNotAvailableException("Chat Not Found"));

        if(chat.getType()!= Chat.ChatType.DIRECT) {
            return;
        }

        List<String> memberIds = chat.getMembers().stream()
                .map(m -> m.getId().getUserId())
                .filter(id -> !id.equals(userId))
                .toList();

        ActivityMaps activityMaps=instanceRouter.buildActivityMaps(memberIds,chat.getId());
        Map<String, List<String>> inChatMap  = activityMaps.getInChatMap();
        Map<String, List<String>> inInboxMap = activityMaps.getInInboxMap();
        if(!inChatMap.isEmpty()){
            TypingEvent typingEvent=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
                    TypingEvent.start(chatId,userId):TypingEvent.stop(chatId,userId);
            instanceRouter.route(inChatMap,ids->new TypingDelivery(ids,typingEvent));
        }
        if(!inInboxMap.isEmpty()){
            InboxEvent inboxEvent=typingPayload.getTypingEventType()== TypingEvent.TypingEventType.TYPING_START?
                    InboxEvent.typingMessageStarted(chatId,userId):InboxEvent.typingMessageStopped(chatId,userId);
            instanceRouter.route(inInboxMap,ids->new InboxDelivery(ids,inboxEvent));
        }


    }
}
