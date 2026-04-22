package com.example.SocialMediaApp.Notification.application;


import com.example.SocialMediaApp.Notification.Configurations.EmailSendingProperties;
import com.example.SocialMediaApp.Notification.Configurations.EmailSendingWebClient;
import com.example.SocialMediaApp.Notification.api.dto.EmailSendingRequest;
import com.example.SocialMediaApp.Notification.domain.EmailSending;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentNotificationService {

    private final EmailSendingProperties emailSendingProperties;

    private final WebClient webClient;

    public void sendEmail(EmailSending emailSending){
        Map<String,String> sender=Map.of("sender", emailSendingProperties.getSenderEmail());

        EmailSendingRequest request=EmailSendingRequest.builder().
                sender(sender).to(emailSending.getTo()).
                htmlContent(emailSending.getHtmlContent()).build();

        webClient.post().uri("/smtp/email").bodyValue(request).retrieve().toBodilessEntity().block();
    }

}

