package com.Nexsta.Messaging.api.Controllers;

import com.Nexsta.Messaging.api.dto.ChatPreview;
import com.Nexsta.Messaging.api.dto.ChatUsers;
import com.Nexsta.Messaging.api.dto.ChatPage;
import com.Nexsta.Messaging.api.dto.MessagePage;
import com.Nexsta.Messaging.application.ChatViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/view")
@Validated
@Slf4j
public class ChatViewController {

    private final ChatViewService chatViewService;

    @GetMapping
    public ResponseEntity<ChatPage> getChats(@RequestParam(required = false)Instant cursor){
        return ResponseEntity.ok(chatViewService.getUserChats(cursor));
    }

    @GetMapping("/{chatId}/v")
    public ResponseEntity<ChatPreview> getChat(@PathVariable String chatId){
        return ResponseEntity.ok(chatViewService.getUserChat(chatId));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatUsers> getChatDetails(@PathVariable String chatId){

        return ResponseEntity.ok(chatViewService.getChatDetails(chatId));
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<MessagePage> getChatMessages(@PathVariable String chatId, @RequestParam(required = false) String cursor){

        return ResponseEntity.ok(chatViewService.getChatMessages(chatId,cursor));
    }


}
