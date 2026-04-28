package com.Nexsta.Upload.api.Controllers;

import com.Nexsta.Upload.api.dto.UploadRequest;
import com.Nexsta.Upload.api.dto.UploadResponse;
import com.Nexsta.Upload.application.UploadGatewayService;
import com.Nexsta.Upload.domain.SupabaseWebhookPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
public class UploadGatewayController {

    private final UploadGatewayService uploadGatewayService;


    @PostMapping
    public ResponseEntity<UploadResponse> requestUpload(@AuthenticationPrincipal(expression = "subject") String currentUserId, @RequestBody @Valid UploadRequest uploadRequest){
        return ResponseEntity.ok(uploadGatewayService.requestUpload(currentUserId,uploadRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void>  discardUpload(@AuthenticationPrincipal(expression = "subject") String currentUserId,@RequestParam String requestId){
        uploadGatewayService.discardUpload(currentUserId, requestId);
        return ResponseEntity.noContent().build();
    }

    //
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmUpload(@RequestHeader("X-Webhook-Secret") String signature,@RequestBody SupabaseWebhookPayload webhookPayload){
        uploadGatewayService.confirmUpload(signature,webhookPayload);
        return ResponseEntity.noContent().build();
    }

}
