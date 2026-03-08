package com.example.SocialMediaApp.Messaging.application;

import com.example.SocialMediaApp.Messaging.Exceptions.ChatMessagingException;
import com.example.SocialMediaApp.Messaging.api.dto.*;
import com.example.SocialMediaApp.Messaging.domain.Chat;
import com.example.SocialMediaApp.Messaging.domain.ChatMember;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Messaging.persistence.ChatMemberRepo;
import com.example.SocialMediaApp.Messaging.persistence.ChatRepo;
import com.example.SocialMediaApp.Messaging.persistence.MessageRepo;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Profile.application.ProfileSummaryBuilder;
import com.example.SocialMediaApp.Profile.application.cache.ProfileCacheManager;
import com.example.SocialMediaApp.Profile.domain.Profile;
import com.example.SocialMediaApp.Shared.Mappers.Chatmapper;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import com.example.SocialMediaApp.User.application.UserActivityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatOverviewService {

    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileSummaryBuilder profileSummaryBuilder;
    private final ChatRepo chatRepo;
    private final Chatmapper chatmapper;
    private final ChatStatusResolver chatStatusResolver;
    private final MessageRepo messageRepo;
    private final UserActivityTracker userActivityService;
    private final ProfileCacheManager profileCacheManager;

    public List<ChatSummary> getUserChats(int page){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Pageable pageable= PageRequest.of(page,10);
        Page<Chat> Page = chatRepo.findByUserId(currentUserId,pageable);
        List<Chat> chats = Page.getContent();
        List<chatMemberDTO> otherChatMembers=chatMemberRepo.findOtherChatMembers(chats.stream().map(Chat::getId).toList(),currentUserId);
        List<ProfileSummary> profileSummaries= profileSummaryBuilder.buildProfileSummaries(otherChatMembers.stream().map(chatMemberDTO::getUser_Id).toList());
        Map<String,chatMemberDTO> map=otherChatMembers.stream()
                .collect(Collectors.toMap(chatMemberDTO::getUser_Id, Function.identity()));
        List<ChatSummary> chatSummaries= profileSummaries.stream().map(profileSummary -> {
            chatMemberDTO chatMemberDTO=map.get(profileSummary.getUserId());
            ChatSummary chatDTO=chatmapper.tochatDTO(profileSummary);
            chatDTO.setChatId(chatMemberDTO.getChat_Id());
            return chatDTO;
        }).toList();
        chatStatusResolver.computeStatus(chats,chatSummaries,currentUserId);
        return chatSummaries;
    }

    public ChatDetails getUserChat(String chatId, int page){
        String currentUserId=authenticatedUserService.getCurrentUser();

        if(!chatRepo.existsById(chatId)||!chatMemberRepo.existsByUserIdAndChatId(currentUserId,chatId)){
            throw new ChatMessagingException("Chat not found");
        }

        ChatMember otherchatMember=chatMemberRepo.findByChatIdAndUserIdNot(chatId,currentUserId).orElseThrow();
        ChatUser chatUser=buildChatUser(otherchatMember);
        Pageable pageable=PageRequest.of(page,10, Sort.by(Sort.Direction.DESC,"lastMessageAt"));
        Page<Message> Page= messageRepo.findByChatId(chatId,pageable);
          List<Message> messages= Page.getContent();
          List<MessageDTO> messageDTOS=new ArrayList<>();

          for (Message message:messages){
              MessageDTO messageDTO=chatmapper.tomessageDTO(message);

              messageDTO.setSentByme(message.getSenderId().equals(currentUserId));

              if(message.getId().equals(otherchatMember.getLastreadMessageId())){
                  messageDTO.setLastView(true);
              }

              messageDTOS.add(messageDTO);
          }

          if(page==0&&!messages.isEmpty()){
             chatMemberRepo.updateUserChat(messages.get(0).getId(),currentUserId,chatId);
          }

          return new ChatDetails(chatId,chatUser,messageDTOS);
    }
    private ChatUser buildChatUser(ChatMember chatMember){
        String otherchatMemberId=chatMember.getUserId();
        Profile profile=profileCacheManager.getProfile(otherchatMemberId).get();
        Boolean online=null;
        String lastActivity=null;
        if(profile.isShowifonline()){
            if(userActivityService.getUserStatus(otherchatMemberId)){
                online=true;
            }else{
                lastActivity=userActivityService.getUserLastSeen(otherchatMemberId);
            }
        }
        ChatUser chatUser=chatmapper.tochatUser(profile);
        chatUser.setOnline(online);
        chatUser.setLastActivity(lastActivity);
        return chatUser;
    }

}
