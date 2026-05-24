package com.Nexsta.Messaging.api.Controllers;

import com.Nexsta.Messaging.api.dto.ChatDetails;
import com.Nexsta.Messaging.api.dto.ChatPage;
import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.api.dto.MessagePage;
import com.Nexsta.Messaging.application.ChatViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/view")
@Validated
public class ChatViewController {

    private final ChatViewService chatViewService;

    @GetMapping
    public ResponseEntity<ChatPage> getChats(@RequestParam(required = false)Instant cursor){

        return ResponseEntity.ok(chatViewService.getUserChats(cursor));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDetails> getChatDetails(@PathVariable String chatId){

        return ResponseEntity.ok(chatViewService.getChatDetails(chatId));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<MessagePage> getChatMessages(@PathVariable String chatId, @RequestParam(required = false) String cursor){

        return ResponseEntity.ok(chatViewService.getChatMessages(chatId,cursor));
    }


}
