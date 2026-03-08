package com.example.SocialMediaApp.Upload.api.Controllers;

import com.example.SocialMediaApp.Upload.api.dto.UploadRequest;
import com.example.SocialMediaApp.Upload.api.dto.UploadResponse;
import com.example.SocialMediaApp.Upload.application.UploadGatewayService;
import com.example.SocialMediaApp.Upload.domain.SupabaseWebhookPayload;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@Hidden
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;


    @PostMapping
    public ResponseEntity<UploadResponse> requestUpload(@AuthenticationPrincipal(expression = "subject") String currentUserId, @RequestBody @Valid UploadRequest uploadRequest){
        return ResponseEntity.ok(uploadGatewayService.requestUpload(currentUserId,uploadRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void>  discardUpload(@AuthenticationPrincipal(expression = "subject") String currentUserId,@RequestParam String filepath){
        uploadGatewayService.discardUpload(currentUserId,filepath);
        return ResponseEntity.noContent().build();
    }

    //
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmUpload(@RequestHeader("X-Webhook-Secret") String signature, @RequestBody SupabaseWebhookPayload webhookPayload){
        uploadGatewayService.confirmUpload(signature,webhookPayload);
        return ResponseEntity.noContent().build();
    }

}
