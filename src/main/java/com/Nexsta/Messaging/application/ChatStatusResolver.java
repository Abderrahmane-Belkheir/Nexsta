package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.domain.MessageType;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.User.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatusResolver {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;


    public void computeStatus(List<Chat> chats, List<ChatSummary> chatDTOS, String currentId){

        Map<String, ChatSummary> dtomap= chatDTOS.stream()
                .collect(Collectors.toMap(ChatSummary::getChatId, Function.identity()));

        List<String> lastmessageIds=chats.stream().map(Chat::getLastMessageId).collect(Collectors.toList());

        List<Message> lastmessages= messageRepo.findByIdIn(lastmessageIds);

        List<String> unProcessedChats=new ArrayList<>();
        // Process the last message of each chat:
// - If the message was sent by the current user, show whether it was seen or just sent.
// - If the message was sent by someone else and has been read by the current user, display the message content.
// - Otherwise, add the chat to `unProcessedChats` for further processing.

        for(Message message:lastmessages){
            ChatSummary chatDTO=dtomap.get(message.getChatId());

            if(chatDTO==null) continue;
            if(message.getSenderId().equals(currentId)){
                if(message.isRead()){
                    chatDTO.setChatPreview("seen:"+message.getReadAt());
                }else{
                    chatDTO.setChatPreview("sent:"+message.getSentAt());
                }
            }else if(message.isRead()){
                if(message.getMessageType()== MessageType.TEXT){
                    chatDTO.setChatPreview(message.getContent()+":"+message.getSentAt());
                }
            }else {
                unProcessedChats.add(message.getChatId());
            }
        }

        if(unProcessedChats.isEmpty()) return;

        List<ChatMember> chatMembers= chatMemberRepo.findByUserAndChatIdIn(new User(currentId),unProcessedChats);

        // Process chats with unread messages
         // Update each chatDTO to show the current user how many new messages are in the chat.

        for(ChatMember chatMember:chatMembers){
            String chatId=chatMember.getChatId();
            ChatSummary chatDTO= dtomap.get(chatId);

            if(chatDTO==null) continue;

            chatDTO.setChatPreview("+"+chatMember.getUnreadCount()+" New Messages");
        }

    }
}

