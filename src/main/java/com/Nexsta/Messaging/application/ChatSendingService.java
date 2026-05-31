package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.Exceptions.ChatMessagingException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.ChatMember;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatMemberRepo;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.Profile.application.ProfileQueryService;
import com.Nexsta.Profile.domain.Profile;
import com.Nexsta.Profile.domain.cache.ProfileInfo;
import com.Nexsta.Shared.CheckUserExistence;
import com.Nexsta.Shared.ServerInstance;
import com.Nexsta.SocialGraph.domain.Follow;
import com.Nexsta.SocialGraph.persistence.BlocksRepo;
import com.Nexsta.SocialGraph.persistence.FollowRepo;
import com.Nexsta.User.application.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


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
    private final RealTimeDeliveringService realTimeDeliveringService;
    private final RedisTemplate<String,String> redisTemplate;
    private final ServerInstance serverInstance;

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

            Optional<String> recipientInstanceId=chatActivityTracker.isUserActiveInInbox(recipientId);
        if(recipientInstanceId.isPresent()){
            ProfileInfo profileInfo=profileQueryService.getUserProfileInfo(currentUserId);
            ChatSummary chatSummary=ChatSummary.builder().chatId(id).chatType(Chat.ChatType.DIRECT).chatName(profileInfo.getUsername()).chatAvatar(profileInfo.getAvatarPath()).preview(ChatPreview.unread(1)).build();
            realTimeDeliveringService.deliverInboxEvent(new InboxDelivery(List.of(recipientId),InboxEvent.newChat(chatSummary)));
        }

        Optional<String> senderInstanceId=chatActivityTracker.isUserActiveInInbox(currentUserId);
        if(senderInstanceId.isPresent()){
            ProfileInfo profileInfo=profileQueryService.getUserProfileInfo(recipientId);
            ChatSummary chatSummary=ChatSummary.builder().chatId(id).chatType(Chat.ChatType.DIRECT).chatName(profileInfo.getUsername()).chatAvatar(profileInfo.getAvatarPath()).preview(ChatPreview.sent(message.getSentAt())).build();
            realTimeDeliveringService.deliverInboxEvent(new InboxDelivery(List.of(currentUserId),InboxEvent.newChat(chatSummary)));
        }

        }catch (Exception e){
            throw new ChatMessagingException("could not send message to user");
        }
    }

    private void processAndDeliverMessage(Chat chat, String currentUserId, SendMessage messageDTO){
        Message message=messageRepo.save(new Message(chat.getId(),currentUserId, messageDTO.getContent()));
        chat.setLastMessageId(message.getId());
        chat.setLastMessageAt(message.getSentAt());

        if(!chat.getMembers().isEmpty()){

            Map<String,List<String>> activeUsersInChatMap=new HashMap<>();
            Map<String,List<String>> activeUsersInInboxMap =new HashMap<>();
            List<ChatMember> members=chat.getMembers().stream().filter(chatMember -> !chatMember.getId().getUserId().equals(currentUserId)).toList();
            log.info("chat members"+members.get(0).getId().getUserId());
            for(ChatMember member:members){
                String memberId=member.getId().getUserId();
                Optional<String> memberInstanceId=chatActivityTracker.isUserActiveInInbox(memberId);

                if(memberInstanceId.isPresent()){
                    log.info("instance id "+memberInstanceId.get());
                    if(chatActivityTracker.isUserActiveInChat(memberId,chat.getId())){
                        activeUsersInChatMap.computeIfAbsent(memberInstanceId.get(), k -> new CopyOnWriteArrayList<>()).add(memberId);
                    }else{
                        activeUsersInInboxMap.computeIfAbsent(memberInstanceId.get(), k -> new CopyOnWriteArrayList<>()).add(memberId);
                    }
                }else{
                    log.info("instance id is null");
                }
            }

            List<String> activeUsersInChat=activeUsersInChatMap.values().stream().flatMap(List::stream).toList();
            chatMemberRepo.incrementUnReadCount(chat.getId(),currentUserId,activeUsersInChat);
            chatMemberRepo.resetCountAndUpdateLastReadMessage(chat.getId(),currentUserId,message.getId());

            if(!activeUsersInInboxMap.isEmpty()){
                InboxEvent event=InboxEvent.newMessage(chat.getId());
                for(Map.Entry<String,List<String>> entry:activeUsersInInboxMap.entrySet()){
                    String membersInstanceId=entry.getKey();
                    InboxDelivery inboxDelivery=new InboxDelivery(entry.getValue(),event);
                    if(membersInstanceId.equals(serverInstance.getInstanceId())){
                        realTimeDeliveringService.deliverInboxEvent(inboxDelivery);
                    }else {
                        redisTemplate.convertAndSend(membersInstanceId,inboxDelivery);
                    }
                }
            }

            MessageView messageView=new MessageView(message.getId(),message.getChatId(),message.getSenderId(),message.getContent(),message.getSentAt(),true, activeUsersInChat);
            realTimeDeliveringService.deliverMessage(new MessageDelivery(List.of(currentUserId),messageView));

            if(!activeUsersInChatMap.isEmpty()) {
                messageView.setMine(false);
                messageView.setSeenByUserIds(null);
                for(Map.Entry<String,List<String>> entry:activeUsersInChatMap.entrySet()){
                    String membersInstanceId=entry.getKey();
                    MessageDelivery messageDelivery=new MessageDelivery(entry.getValue(),messageView);
                    if(membersInstanceId.equals(serverInstance.getInstanceId())){
                        realTimeDeliveringService.deliverMessage(messageDelivery);
                    }else {
                        log.info("sending message to "+messageDelivery);
                        redisTemplate.convertAndSend(membersInstanceId,messageDelivery);
                    }
                }

                realTimeDeliveringService.deliverInboxEvent(new InboxDelivery(List.of(currentUserId),InboxEvent.readReceipt(chat.getId(), activeUsersInChat)));
                }else{
                    realTimeDeliveringService.deliverInboxEvent(new InboxDelivery(List.of(currentUserId),InboxEvent.sentMessage(chat.getId())));
                }

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
