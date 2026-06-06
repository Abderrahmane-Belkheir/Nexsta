package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageInteractionService {

    private final AuthenticatedUserService authenticatedUserService;
    private final MessageRepo messageRepo;
    private final ChatRepo chatRepo;
    private final InstanceRouter instanceRouter;

    @Transactional
    public void removeMessage(RemoveMessage removeMessage){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Message message=messageRepo.findByIdAndSenderIdAndDeletedFalse(removeMessage.getMessageId(),currentUserId).orElseThrow(()->new ContentNotAvailableException(""));
        Chat chat=chatRepo.findChatById(message.getChatId(),currentUserId).orElseThrow();
        List<String> membersId=chat.getMembers().stream().map(chatMember -> chatMember.getId().getUserId()).toList();
        if(removeMessage.getRemoveType()== RemoveMessage.RemoveType.FOR_ME){
            message.setDeleted(true);
            messageRepo.save(message);
            instanceRouter.deliverLocally(new MessageRemovedDelivery(List.of(currentUserId),message.getId()));
        }else{
            chat.getMembers().stream().filter(m -> m.getLastReadMessageId() == null ||
                    m.getLastReadMessageId().compareTo(message.getId()) < 0).forEach(chatMember ->chatMember.setUnReadCount(Math.max(0,chatMember.getUnReadCount()-1)));
            if(chat.getLastMessageId().equals(message.getId())){
                messageRepo.findFirstByChatIdAndIdLessThanOrderByIdDesc(chat.getId(),new ObjectId(message.getId()))
                        .ifPresentOrElse(prev -> {
                            chat.setLastMessageId(prev.getId());
                            chat.setLastMessageAt(prev.getSentAt());
                        }, () -> {
                            chat.setLastMessageId(null);
                            chat.setLastMessageAt(null);
                        });
            }
            messageRepo.delete(message);
            ActivityMaps activityMaps=instanceRouter.buildActivityMaps(membersId,chat.getId());
            Map<String,List<String>> inChatMap=activityMaps.getInChatMap();
            instanceRouter.routeBatch(inChatMap, ids ->
                    new MessageRemovedDelivery(ids, message.getId()));
        }

    }

    public void reactToMessage(){

    }

}
