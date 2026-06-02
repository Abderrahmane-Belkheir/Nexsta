package com.Nexsta.Messaging.application;

import com.Nexsta.Messaging.api.dto.*;
import com.Nexsta.Shared.ServerInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstanceRouter {

    private final ChatActivityTracker chatActivityTracker;
    private final RealTimeDeliveringService realTimeDeliveringService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ServerInstance serverInstance;


    public ActivityMaps buildActivityMaps(List<String> userIds, String chatId) {
        Map<String, List<String>> inChatMap  = new HashMap<>();
        Map<String, List<String>> inInboxMap = new HashMap<>();

        userIds.forEach(id -> {
            Optional<String> memberInChatInstanceId =chatActivityTracker.isUserActiveInChat(id,chatId);
                 memberInChatInstanceId.ifPresentOrElse(s -> inChatMap.computeIfAbsent(s, k -> new CopyOnWriteArrayList<>()).add(id),()->{
                    Optional<String> memberInInboxInstanceId=chatActivityTracker.isUserActiveInInbox(id);
                    memberInInboxInstanceId.ifPresent(s->inInboxMap.computeIfAbsent(s, k -> new CopyOnWriteArrayList<>()).add(id));
                });
        });

        return new ActivityMaps(inChatMap, inInboxMap);
    }


    public void route(Map<String, List<String>> instanceMap, Function<List<String>, BaseDelivery> deliveryFactory) {
        if(instanceMap.isEmpty()) return;

        instanceMap.forEach((instanceId, userIds) -> {
            BaseDelivery delivery = deliveryFactory.apply(new ArrayList<>(userIds));
            if (instanceId.equals(serverInstance.getInstanceId())) {
                deliverLocally(delivery);
            } else {
                redisTemplate.convertAndSend(instanceId, delivery);
            }
        });
    }

    public void routeToSingle(String userId, BaseDelivery delivery) {
        chatActivityTracker.isUserActiveInInbox(userId).ifPresent(instanceId -> {
            if (instanceId.equals(serverInstance.getInstanceId())) {
                deliverLocally(delivery);
            } else {
                redisTemplate.convertAndSend(instanceId, delivery);
            }
        });
    }

    public void deliverLocally(BaseDelivery delivery) {
        if (delivery instanceof MessageDelivery m) realTimeDeliveringService.deliverMessage(m);
        else if (delivery instanceof InboxDelivery i) realTimeDeliveringService.deliverInboxEvent(i);
        else if (delivery instanceof TypingDelivery t) realTimeDeliveringService.deliverTypingEvent(t);
        else log.warn("Unknown delivery type: {}", delivery.getClass().getSimpleName());
    }

}
