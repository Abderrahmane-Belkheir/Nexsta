package com.Nexsta.Messaging.application;

import com.Nexsta.Content.Exceptions.ContentNotAvailableException;
import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Messaging.domain.Chat;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Messaging.persistence.ChatRepo;
import com.Nexsta.Messaging.persistence.MessageRepo;
import com.Nexsta.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class MessageInteractionService {

    private final AuthenticatedUserService authenticatedUserService;
    private final MessageRepo messageRepo;
    private final ChatRepo chatRepo;
    private InstanceRouter instanceRouter;

    public void removeMessage(RemoveMessage removeMessage){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Message message=messageRepo.findByIdAndSenderIdAndDeletedFalse(removeMessage.getMessageId(),currentUserId).orElseThrow(()->new ContentNotAvailableException(""));
        Chat chat=chatRepo.findChatById(message.getChatId(),currentUserId).orElseThrow();
        List<String> membersId=chat.getMembers().stream().map(chatMember -> chatMember.getId().getUserId()).toList();
        if(removeMessage.getRemoveType()== RemoveMessage.RemoveType.FOR_ME){
            message.setDeleted(true);
            messageRepo.save(message);
            instanceRouter.routeToSingle(currentUserId,, InstanceRouter.SingleRoutingType.CHAT,chat.getId());
        }else{
            ActivityMaps activityMaps=instanceRouter.buildActivityMaps(membersId,chat.getId());
            Map<String,List<String>> inChatMap=activityMaps.getInChatMap();
            instanceRouter.route(inChatMap,ids ->
                    new MessageRemovedDelivery(ids, message.getId()));
        }

    }

    public void reactToMessage(){

    }

}
