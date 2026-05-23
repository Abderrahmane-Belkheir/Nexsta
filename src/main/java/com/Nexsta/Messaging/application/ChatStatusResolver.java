package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.ChatPreview;
import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatAggregate;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatusResolver {

    private final MessageRepo messageRepo;



    public void computeStatus(Map<String, ChatAggregate> chatAggregateMap, String currentUserId){

        for (ChatAggregate aggregate : chatAggregateMap.values()) {

            ChatSummary summary = aggregate.getSummary();
            // get current user member directly from already loaded members
            ChatMember currentMember = aggregate.getMembers().stream()
                    .filter(m -> m.getUserId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow();

            if (currentMember.getUnreadCount() > 0) {
                summary.setPreview(ChatPreview.unread(currentMember.getUnreadCount()));
            }

        }

        List<String> lastMessagesIds =chatAggregateMap.values().stream().
                filter(chatAggregate -> chatAggregate.getSummary().getPreview()==null).
                map(chatAggregate -> chatAggregate.getChat().getLastMessageId()).toList();

        if(lastMessagesIds.isEmpty()) return;

        List<Message> lastMessages= messageRepo.findByIdIn(lastMessagesIds);

        for (Message message : lastMessages) {
            ChatAggregate aggregate = chatAggregateMap.get(message.getChatId());
            if (aggregate == null) continue;

            ChatSummary summary = aggregate.getSummary();

            if (!message.getSenderId().equals(currentUserId)) {
                // other user sent it
                summary.setPreview(ChatPreview.received(message.getContent()));
            } else {
                // current user sent it — check if others saw it
                List<String> seenBy = aggregate.getMembers().stream()
                        .filter(m -> !m.getUserId().equals(currentUserId))
                        .filter(m -> m.getUnreadCount() == 0)
                        .map(ChatMember::getAvatarUrl)
                        .toList();

                summary.setPreview(seenBy.isEmpty()
                        ? ChatPreview.sent()
                        : ChatPreview.seen(seenBy));
            }

        }

    }
}

