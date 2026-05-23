package com.Nexsta.Messaging.api.Controllers;

import com.Nexsta.Messaging.api.dto.ChatDetails;
import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.application.ChatOverviewService;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/overview")
@Validated
public class ChatOverviewController {

    private final ChatOverviewService chatOverviewService;

    @GetMapping
    public List<ChatSummary> getChats(@RequestParam(defaultValue = "null")Instant cursor){
        return chatOverviewService.getUserChats(cursor);
    }

    @GetMapping("/{chatId}")
    public ChatDetails getChat(@PathVariable String chatId,
                               @RequestParam(defaultValue = "0") @PositiveOrZero  int page){

        return chatOverviewService.getUserChat(chatId,page);
    }


}
