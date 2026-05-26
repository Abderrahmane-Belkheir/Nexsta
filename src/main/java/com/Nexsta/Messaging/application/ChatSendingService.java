package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.Exceptions.ChatMessagingException;
import com.Nexsta.Messaging.api.dto.MessageView;
import com.Nexsta.Messaging.api.dto.SendMessage;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.Profile.application.ProfileQueryService;
import com.Nexsta.Profile.application.cache.ProfileCacheManager;
import com.Nexsta.Profile.domain.Profile;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import com.Nexsta.User.application.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatSendingService {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final BlocksRepo blocksRepo;
    private final ProfileQueryService profileQueryService;
    private final ChatRepo chatRepo;
    private final FollowRepo followRepo;
    private final ChatActivityTracker chatActivityTracker;
    private final MessageDeliveringService messageDeliveringService;


    @CheckUserExistence
    public void sendMessageRouting(SendMessage messageDTO){
        if(messageDTO.getChatId()!=null){
            sendMessageByChatId(messageDTO);
        }else{
            sendMessageByUserId(messageDTO);
        }
    }

    // this is meant for first time chatting it first check whether a chat exists between users or not
    private void sendMessageByUserId(SendMessage messageDTO){
        String currentUserId=authenticatedUserService.getCurrentUser();
        String recipientId=messageDTO.getRecipientId();
        isAllowedToSendUser(currentUserId,recipientId);
        Optional<Chat> optionalChat =chatRepo.findChatBetween(currentUserId,recipientId);

        // if there is a chat between users delegate to sendMessageToChat method
        if(optionalChat.isPresent()){
            Chat chat=optionalChat.get();
            processAndDeliverMessage(chat,currentUserId,messageDTO);
            return ;
        }

        try{
            String id= UUID.randomUUID().toString();
            Chat newChat = new Chat(id);
            newChat.setType(Chat.ChatType.DIRECT);

            ChatMember chatMember1 = new ChatMember(newChat, currentUserId);
            ChatMember chatMember2 = new ChatMember(newChat, recipientId);
            chatMember2.setUnReadCount(1);
            newChat.getMembers().addAll(List.of(chatMember1, chatMember2));

            Message message = messageRepo.save(new Message(newChat.getId(), currentUserId, messageDTO.getContent()));

            newChat.setLastMessageId(message.getId());
            newChat.setLastMessageAt(message.getSentAt());

            chatRepo.save(newChat);

      //  if(chatActivityTracker.isUserActiveInInbox(recipientId))messageDeliveringService.deliverMessage(null,null);

        }catch (Exception e){
            log.error("sending message to user failed "+e.getMessage());
            throw new ChatMessagingException("could not send message to user");
        }
    }

    private void processAndDeliverMessage(Chat chat, String currentUserId, SendMessage messageDTO){
        Message message=messageRepo.save(new Message(chat.getId(),currentUserId, messageDTO.getContent()));
        chat.setLastMessageId(message.getId());
        chat.setLastMessageAt(message.getSentAt());

        if(!chat.getMembers().isEmpty()){
            List<String> activeUsersInChat=new ArrayList<>();
            for(ChatMember member:chat.getMembers()){
                String memberId=member.getId().getUserId();
                if(chatActivityTracker.isUserActiveInChat(memberId,chat.getId())){
                    log.info("{} is active in chat",memberId);
                    activeUsersInChat.add(memberId);
                }
            }
            if(!activeUsersInChat.isEmpty()) {
                log.info("delevering message");
                messageDeliveringService.deliverMessage(activeUsersInChat,message);
            }

            chatMemberRepo.incrementUnReadCount(chat.getId(),currentUserId,activeUsersInChat);
            chatMemberRepo.resetCountAndUpdateLastReadMessage(chat.getId(),currentUserId,message.getId());
    }

    }

    private void sendMessageByChatId(SendMessage messageDTO){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Chat chat=chatRepo.findChatById(messageDTO.getChatId(),currentUserId).orElseThrow(()->new ContentNotAvailableException("Chat Not Found"));
        processAndDeliverMessage(chat,currentUserId,messageDTO);
    }


    private void isAllowedToSendUser(String currentUserId, String recipientId){
        if(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,recipientId)||blocksRepo.existsByBlockerIdAndBlockedId(recipientId,currentUserId)){
            throw new ChatMessagingException("cannot send message to user");
        }
        Profile profile=profileQueryService.getUserProfile(currentUserId,true);
        if(profile.getProfileSettings().isPrivate()){
            boolean followed=followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,recipientId,Follow.Status.ACCEPTED);
            if(!followed){
                throw new ChatMessagingException("cannot send message to user");
            }
        }
    }

}
