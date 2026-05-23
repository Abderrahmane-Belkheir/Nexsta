package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.Exceptions.ChatMessagingException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatAggregate;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.Nexsta.Profile.application.ProfileSummaryBuilder;
import com.Nexsta.Profile.application.cache.ProfileCacheManager;
import com.Nexsta.Profile.domain.Profile;
import com.Nexsta.Shared.Mappers.Chatmapper;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.User.application.UserActivityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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


    public List<ChatSummary> getUserChats(Instant cursor){
    String currentUserId = authenticatedUserService.getCurrentUser();
    Pageable pageable = PageRequest.of(0, 11);

    // 1. paginate
    List<String> chatsId = chatRepo.findUserChatIds(currentUserId, cursor, pageable);
    boolean hasMore = chatsId.size() > 10;
    if (hasMore) chatsId = chatsId.subList(0, 10);
    if (chatsId.isEmpty()) return List.of();


    List<Chat> chats = chatRepo.findChatsByIds(chatsId);

    List<ChatMember> otherChatMembers=chats.stream()
            .flatMap(chat -> chat.getMembers().stream())
            .filter(m -> !m.getUserId().equals(currentUserId))
            .toList();


    List<ProfileSummary> profileSummaries = profileSummaryBuilder.buildProfileSummaries(
            otherChatMembers.stream().map(ChatMember::getUserId).toList()
    );


    Map<String, ChatMember> memberByUserId = otherChatMembers.stream()
            .collect(Collectors.toMap(ChatMember::getUserId, Function.identity()));


    Map<String, Chat> chatMap = chats.stream()
            .collect(Collectors.toMap(Chat::getId, Function.identity()));


    Map<String, ChatAggregate> aggregateMap = new HashMap<>();

    List<ChatSummary> chatSummaries = profileSummaries.stream().map(profileSummary -> {
        ChatMember otherMember = memberByUserId.get(profileSummary.getUserId());
        String chatId = otherMember.getChatId();
        Chat chat = chatMap.get(chatId);

        ChatSummary summary = ChatSummary.builder()
                .chatId(chatId)
                .avatarUrl(profileSummary.getAvatarurl())
                .build();

        // populate aggregate with chat and members only
        aggregateMap.put(chatId, new ChatAggregate(chat, summary, chat.getMembers()));

        return summary;
    }).toList();

    // 7. resolve preview for each chat using aggregate map
    chatStatusResolver.computeStatus(aggregateMap, currentUserId);

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

          }

          return new ChatDetails(chatId,chatUser,messageDTOS);
    }
    private ChatUser buildChatUser(ChatMember chatMember){
        String otherchatMemberId=chatMember.getUserId();
        Profile profile=profileCacheManager.getProfile(otherchatMemberId).get();
        Boolean online=null;
        String lastActivity=null;
        if(profile.getProfileSettings().isShowActivity()){
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
