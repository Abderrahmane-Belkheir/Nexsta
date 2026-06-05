package com.Nexsta.Messaging.api.Controllers;



import com.Nexsta.Messaging.api.dto.RemoveMessage;
import com.Nexsta.Messaging.application.MessageInteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/messages")
public class MessageInteractionController {

    private final MessageInteractionService messageInteractionService;

    @DeleteMapping
    public ResponseEntity<Void> removeMessage(@RequestBody @Valid RemoveMessage removeMessage){
        messageInteractionService.removeMessage(removeMessage);
        return ResponseEntity.noContent().build();
    }

}

