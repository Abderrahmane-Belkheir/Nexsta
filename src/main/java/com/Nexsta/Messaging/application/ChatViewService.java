package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
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
import com.Nexsta.Shared.Mappers.Chatmapper;
import com.Nexsta.User.application.AuthenticatedUserService;
import com.Nexsta.User.application.UserActivityTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatViewService {

    private final ChatMemberRepo chatMemberRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ProfileSummaryBuilder profileSummaryBuilder;
    private final ChatRepo chatRepo;
    private final Chatmapper chatmapper;
    private final ChatStatusResolver chatStatusResolver;
    private final MessageRepo messageRepo;
    private final UserActivityTracker userActivityService;


    private final static int messagePageLimit=20;
    private final static int chatPageLimit=10;



    public ChatPage getUserChats(Instant cursor){

    String currentUserId = authenticatedUserService.getCurrentUser();

    Pageable pageable = PageRequest.of(0, chatPageLimit+1);

    List<Chat> chats = cursor==null?chatRepo.findLastestChats(currentUserId,pageable):chatRepo.findUserChatIds(currentUserId, cursor, pageable);

    boolean hasMore = chats.size() > chatPageLimit;

    if (hasMore){
        chats = chats.subList(0, chatPageLimit);
    }

       List<String> directChats =chats.stream().filter(chat -> chat.getType()== Chat.ChatType.DIRECT).map(Chat::getId).toList();
       directChats.forEach(System.out::println);
       List<ChatMemberRepo.ChatMember> m=chatMemberRepo.findOtherChatMember(directChats,currentUserId);

        Map<String, String> otherMemberByChatId = m.stream()
                .collect(Collectors.toMap(
                        ChatMemberRepo.ChatMember::getChatId,
                        ChatMemberRepo.ChatMember::getUserId
                ));

    Map<String,ProfileSummary> profileSummaries = profileSummaryBuilder.buildProfileSummaries(
           m.stream().map(ChatMemberRepo.ChatMember::getUserId).toList()
    ).stream().collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));


    Map<String, ChatAggregate> aggregateMap = new HashMap<>();

    List<ChatSummary> chatSummaries = chats.stream().map(chat -> {
        String chatId=chat.getId();
        ChatSummary summary = ChatSummary.builder()
                .chatId(chat.getId())
                .chatType(chat.getType())
                .build();

        if(chat.getType()== Chat.ChatType.DIRECT) {
            String memberId= otherMemberByChatId.get(chatId);
            ProfileSummary profileSummary=profileSummaries.get(memberId);
            if(profileSummary!=null){
                summary.setChatName(profileSummary.getUsername());
                summary.setChatAvatar(profileSummary.getAvatarurl());
                summary.setActive(userActivityService.getUserStatus(profileSummary.getUserId()));
                summary.setLastSeen(null);
            }
        }else{
            summary.setChatName(chat.getName());
            summary.setChatAvatar(chat.getAvatarUrl());
        }

        aggregateMap.put(chatId, new ChatAggregate(chat, summary));

        return summary;
    }).toList();


    chatStatusResolver.computeStatus(aggregateMap, currentUserId);

    return ChatPage.builder()
            .chats(chatSummaries)
            .build();
    }

    public ChatUsers getChatDetails(String chatId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Chat chat=chatRepo.findChatById(chatId,currentUserId).orElseThrow(()->new ContentNotAvailableException("Chat Not Found"));
        List<ChatMember> members=chat.getMembers();
        List<ChatUser> chatUsers=buildChatUsers(members);
        return ChatUsers.builder().chatUsers(chatUsers).build();
    }

    public MessagePage getChatMessages(String chatId,String cursor){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Pageable pageable=PageRequest.of(0,messagePageLimit+1);

        List<Message> messages= cursor==null?
                messageRepo.findByChatIdOrderByIdDesc(chatId,pageable):
                messageRepo.findByChatIdAndIdLessThanOrderByIdDesc(chatId,currentUserId,pageable);

        boolean hasMore = messages.size() > messagePageLimit;
        if (hasMore) messages = messages.subList(0, messagePageLimit);
        if (messages.isEmpty()) return null;

        List<MessageView> messagesView =new ArrayList<>();

        for (Message message:messages){
            MessageView messageView=chatmapper.toMessageView(message);

            messageView.setMine(message.getSenderId().equals(currentUserId));

            messagesView.add(messageView);
        }

        if(cursor==null){
          chatMemberRepo.resetCountAndUpdateLastReadMessage(chatId,currentUserId,messagesView.get(0).getId());

        }

        Collections.reverse(messagesView);

        return MessagePage.builder().
                messages(messagesView).
                oldestCursor(null).
                newestCursor(null).
                build();
    }

    private List<ChatUser> buildChatUsers(List<ChatMember> chatMembers){
      Map<String,ProfileSummary> profileSummaries=profileSummaryBuilder.buildProfileSummaries(chatMembers.stream().
              map(chatMember -> chatMember.getId().getUserId()).toList()).stream().collect(Collectors.toMap(ProfileSummary::getUserId, Function.identity()));

      return chatMembers.stream().map(chatMember ->{
          String userId=chatMember.getId().getUserId();
         ProfileSummary profileSummary=profileSummaries.get(userId);
         boolean isActive=userActivityService.getUserStatus(userId);
         String lastSeen=null;

         if(!isActive) {
             lastSeen=userActivityService.getUserLastSeen(userId);
         }

         return ChatUser.builder().
                 userId(userId).
                 username(profileSummary.getUsername()).
                 avatarPath(profileSummary.getAvatarurl()).
                 lastSeenMessageId(chatMember.getLastReadMessageId()).
                 isActive(isActive).lastActivity(lastSeen).
                 build();

      }).toList();
    }



}
