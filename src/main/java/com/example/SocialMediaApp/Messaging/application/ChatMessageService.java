package com.example.SocialMediaApp.Messaging.application;

import com.example.SocialMediaApp.Messaging.Exceptions.ChatMessagingException;
import com.example.SocialMediaApp.Messaging.api.dto.SendMessageToChatDTO;
import com.example.SocialMediaApp.Messaging.api.dto.SendMessageToUserDTO;
import com.example.SocialMediaApp.Messaging.domain.Chat;
import com.example.SocialMediaApp.Messaging.domain.ChatMember;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Messaging.persistence.ChatMemberRepo;
import com.example.SocialMediaApp.Messaging.persistence.ChatRepo;
import com.example.SocialMediaApp.Messaging.persistence.MessageRepo;
import com.example.SocialMediaApp.Profile.application.cache.ProfileCacheManager;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Shared.CheckUserExistence;
import com.example.SocialMediaApp.SocialGraph.domain.Follow;
import com.example.SocialMediaApp.SocialGraph.persistence.BlocksRepo;
import com.example.SocialMediaApp.SocialGraph.persistence.FollowRepo;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final MessageRepo messageRepo;
    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final BlocksRepo blocksRepo;
    private final ProfileCacheManager profileCacheManager;
    private final ChatRepo chatRepo;
    private final FollowRepo followRepo;
    private final ChatActivityTracker chatActivityTracker;
    private final SimpMessagingTemplate messagingTemplate;

    // this is meant for first time chatting it first check whether a chat exists between users or not
    @CheckUserExistence
    public void sendMessageToUser(SendMessageToUserDTO sendMessageToUserDTO){
        String currentUserId=authenticatedUserService.getCurrentUser();
        String targetUserId=sendMessageToUserDTO.getUserId();
        if(blocksRepo.existsByBlockerIdAndBlockedId(currentUserId,targetUserId)||blocksRepo.existsByBlockerIdAndBlockedId(targetUserId,currentUserId)){
            throw new ChatMessagingException("cannot send message to user");
        }

        Optional<Chat> optionalChat =chatRepo.findChatBetween(currentUserId,targetUserId);

        // if there is a chat between users delegate to sendMessageToChat method
        if(optionalChat.isPresent()){
            Chat chat=optionalChat.get();
            canSendtoUser(currentUserId,targetUserId);
            return ;
        }


        try{
        Chat newChat =chatRepo.save(new Chat());
        ChatMember chatMember1=new ChatMember(newChat,currentUserId);
        ChatMember chatMember2=new ChatMember(newChat,targetUserId);
        chatMember2.incrementUnreadCount();
        chatMemberRepo.saveAll(List.of(chatMember1,chatMember2));
        Message message=messageRepo.save(new Message(newChat.getId(),currentUserId, sendMessageToUserDTO.getContent()));
        newChat.setLastMessageId(message.getId());
        newChat.setLastMessageAt(message.getSentAt());
        chatRepo.save(newChat);
        }catch (Exception e){
            log.error("sending message to user failed "+e.getMessage());
            throw new ChatMessagingException("could not send message to user");
        }
    }



    @CheckUserExistence
    public void sendMessageToChat(Principal principal, SendMessageToChatDTO sendMessageToChatDTO){
        User currentUser=new User(principal.getName());
        ChatMember chatMember=chatMemberRepo.findByChatIdAndUserId(sendMessageToChatDTO.getChatId(),currentUser.getId()).
                orElseThrow(()-> new ChatMessagingException("Chat not found"));
        Chat chat=chatMember.getChat();
        Message message=messageRepo.save(new Message(chat.getId(),currentUser.getId(), sendMessageToChatDTO.getContent()));
        chat.setLastMessageId(message.getId());
        chat.setLastMessageAt(chat.getLastMessageAt());
        Optional<ChatMember> optionalOtherChatMember=chatMemberRepo.findByChatIdAndUserIdNot(chat.getId(),currentUser.getId());
        if(optionalOtherChatMember.isPresent()){
            ChatMember otherChatMember=optionalOtherChatMember.get();
            boolean chatActivity=chatActivityTracker.getChat_UserStatus(currentUser.getId(),chat.getId());
            if(chatActivity){

                otherChatMember.setLastreadMessageId(message.getId());
            }
            otherChatMember.incrementUnreadCount();

        }
    }


    private void canSendtoUser(String currentUserId,String targetUserId){
        Profile profile=profileCacheManager.getProfile(targetUserId).get();
        if(profile.isIsprivate()){
            boolean followed=followRepo.existsByFollowerIdAndFollowingIdAndStatus(currentUserId,targetUserId,Follow.Status.ACCEPTED);
            if(!followed){
                throw new ChatMessagingException("cannot send message to user");
            }
        }
    }
}
