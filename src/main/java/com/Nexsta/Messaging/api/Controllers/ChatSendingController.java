package com.Nexsta.Messaging.api.Controllers;

import com.Nexsta.Messaging.api.dto.SendMessage;
import com.Nexsta.Messaging.application.ChatSendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/send")
public class ChatSendingController {

    private final ChatSendingService chatSendingService;

    @PostMapping
    public ResponseEntity<Void> send(@RequestBody @Valid SendMessage message){
        chatSendingService.sendMessageRouting(message);
        return ResponseEntity.noContent().build();
    }

}
