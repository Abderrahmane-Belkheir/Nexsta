package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.ChatPreview;
import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatAggregate;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatusResolver {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;


    public void computeStatus(Map<String,ChatAggregate> chatAggregateMap,String currentUserId){

    List<ChatMemberRepo.ChatUnread> chatUnReads=chatMemberRepo.
            findUnreadCountsForUser(currentUserId,chatAggregateMap.values().stream().map(chatAggregate -> chatAggregate.getChat().getId()).toList());

    List<String> lastMessagesIds=new ArrayList<>();

           for(ChatMemberRepo.ChatUnread chatUnread:chatUnReads){
               int unReadCount=chatUnread.getUnRead()!=null?chatUnread.getUnRead():0;
        if(unReadCount>0){
            chatAggregateMap.get(chatUnread.getChatId()).getSummary().setPreview(ChatPreview.unread(unReadCount));
        }else{
            Chat chat=chatAggregateMap.get(chatUnread.getChatId()).getChat();
            lastMessagesIds.add(chat.getLastMessageId());
        }
    }

        if(lastMessagesIds.isEmpty()) return;

    List<Message> lastMessages= messageRepo.findByIdIn(lastMessagesIds);
    Map<String, Instant> c=new HashMap<>();

        for (Message message : lastMessages) {
        ChatAggregate aggregate = chatAggregateMap.get(message.getChatId());

        if (aggregate == null) continue;

        ChatSummary summary = aggregate.getSummary();

        if (!message.getSenderId().equals(currentUserId)) {

            summary.setPreview(ChatPreview.received(message.getContent()));
        } else {
            c.put(message.getChatId(),message.getSeenAt());
        }
    }

    List<ChatMemberRepo.ChatMember> p=chatMemberRepo.findMembersWhoSeenLastMessage(c.keySet().stream().toList(),currentUserId);

    Map<String, List<String>> seenByChatId = p.stream()
            .collect(Collectors.groupingBy(
                    ChatMemberRepo.ChatMember::getChatId,
                    Collectors.mapping(ChatMemberRepo.ChatMember::getUserId, Collectors.toList())
            ));

        for(Map.Entry<String,List<String>> chatMap :seenByChatId.entrySet()){
        chatAggregateMap.get(chatMap.getKey()).getSummary().setPreview(ChatPreview.seen(chatMap.getValue(),c.get(chatMap.getKey())));
    }

        chatAggregateMap.values().stream().filter(chatAggregate -> chatAggregate.getSummary().getPreview()==null).forEach(chatAggregate -> {
            Chat chat=chatAggregate.getChat();
            ChatSummary chatSummary=chatAggregate.getSummary();
            chatSummary.setPreview(ChatPreview.sent(chat.getLastMessageAt()));
        });

}
}

