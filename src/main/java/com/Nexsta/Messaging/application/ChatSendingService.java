package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.Exceptions.ChatMessagingException;
import com.Nexsta.Messaging.api.dto.SendMessageDTO;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
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


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatSendingService {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final BlocksRepo blocksRepo;
    private final ProfileCacheManager profileCacheManager;
    private final ChatRepo chatRepo;
    private final FollowRepo followRepo;
    private final ChatActivityTracker chatActivityTracker;
    private final MessageDeliveringService messageDeliveringService;



    public void sendMessageRouting(SendMessageDTO messageDTO){
        if(messageDTO.getChatId()!=null){
            sendMessageByChatId(messageDTO);
        }else{
            sendMessageByUserId(messageDTO);
        }
    }

    // this is meant for first time chatting it first check whether a chat exists between users or not
    @CheckUserExistence
    private void sendMessageByUserId(SendMessageDTO messageDTO){
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
        Chat newChat =chatRepo.save(new Chat());
        ChatMember chatMember1=new ChatMember(newChat,currentUserId);
        ChatMember chatMember2=new ChatMember(newChat,recipientId);
        Message message=messageRepo.save(new Message(newChat.getId(),currentUserId, messageDTO.getContent()));
        newChat.setLastMessageId(message.getId());
        newChat.setLastMessageAt(message.getSentAt());
        newChat.getMembers().addAll(List.of(chatMember1,chatMember2));
        newChat.setType(Chat.ChatType.DIRECT);
        chatRepo.save(newChat);
      //  messageDeliveringService.deliverMessage();
        }catch (Exception e){
            log.error("sending message to user failed "+e.getMessage());
            throw new ChatMessagingException("could not send message to user");
        }
    }

    private void processAndDeliverMessage(Chat chat, String currentUserId, SendMessageDTO messageDTO){
        Message message=messageRepo.save(new Message(chat.getId(),currentUserId, messageDTO.getContent()));
        chat.setLastMessageId(message.getId());
        chat.setLastMessageAt(chat.getLastMessageAt());
        if(!chat.getMembers().isEmpty()){
            List<String> activeUsersInChat=new ArrayList<>();
            for(ChatMember member:chat.getMembers()){
                String memberId=member.getUserId();
                if(chatActivityTracker.isUserActiveInChat(memberId,chat.getId())){
                    activeUsersInChat.add(memberId);
                }
            }

            if(!activeUsersInChat.isEmpty()) {
                messageDeliveringService.deliverMessage(activeUsersInChat,message);
            }
            chatMemberRepo.incrementUnReadCount(chat.getId(),activeUsersInChat);
        }
    }

    @CheckUserExistence
    private void sendMessageByChatId(SendMessageDTO messageDTO){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Chat chat=chatRepo.findChatById(messageDTO.getChatId(),currentUserId).orElseThrow(()->new ContentNotAvailableException("Chat Not Found"));
        processAndDeliverMessage(chat,currentUserId,messageDTO);
    }


    private void isAllowedToSendUser(String currentUserId, String recipientId){
        if(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,recipientId)||blocksRepo.existsByBlockerIdAndBlockedId(recipientId,currentUserId)){
            throw new ChatMessagingException("cannot send message to user");
        }
        Profile profile=profileCacheManager.getProfile(recipientId).get();
        if(profile.getProfileSettings().isPrivate()){
            boolean followed=followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,recipientId,Follow.Status.ACCEPTED);
            if(!followed){
                throw new ChatMessagingException("cannot send message to user");
            }
        }
    }

}
