package com.example.SocialMediaApp.Notification.application;


import com.example.SocialMediaApp.Notification.Configurations.EmailSendingProperties;
import com.example.SocialMediaApp.Notification.api.dto.EmailSendingRequest;
import com.example.SocialMediaApp.Notification.api.dto.EmailSendingResponse;
import com.example.SocialMediaApp.Notification.domain.ContentEmail;
import com.example.SocialMediaApp.Notification.domain.ContentEmailModel;
import com.example.SocialMediaApp.Notification.domain.Email;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ContentNotificationService {

    private final EmailTemplateService emailTemplateService;
    private final EmailSendingProperties emailSendingProperties;
    private final WebClient webClient;

    public ContentNotificationService(EmailTemplateService emailTemplateService,EmailSendingProperties emailSendingProperties,@Qualifier("emailWebClient") WebClient webClient){
        this.emailTemplateService=emailTemplateService;
        this.emailSendingProperties=emailSendingProperties;
        this.webClient=webClient;
    }

    public void sendEmail(Email email){
        Map<String,String> sender=Map.of("sender", emailSendingProperties.getSenderEmail(),"name","MySocialMediaApp");
        String htmlContent=buildHtmlContent(email);
        EmailSendingRequest request=EmailSendingRequest.builder().
                sender(sender).to(email.getTo()).scheduledAt(email.getAt()).
                htmlContent(htmlContent).build();

        webClient.post().uri("/smtp/email")
                .bodyValue(request).retrieve().bodyToMono(EmailSendingResponse.class).block();
    }

    private String buildHtmlContent(Email email){
        String htmlContent=null;

        if(email instanceof ContentEmail contentEmail){
            ContentEmailModel emailModel=ContentEmailModel.builder().postId(contentEmail.getPostId()).scheduledAt(contentEmail.getAt()).build();
            htmlContent=emailTemplateService.buildPostPublishing(emailModel);
        }

        return htmlContent;
    }

}

