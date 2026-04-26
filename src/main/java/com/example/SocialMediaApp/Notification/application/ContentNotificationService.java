package com.example.SocialMediaApp.Notification.application;


import com.example.SocialMediaApp.Notification.Configurations.EmailSendingProperties;
import com.example.SocialMediaApp.Notification.api.dto.EmailSendingRequest;
import com.example.SocialMediaApp.Notification.api.dto.EmailSendingResponse;
import com.example.SocialMediaApp.Notification.domain.ContentEmail;
import com.example.SocialMediaApp.Notification.domain.ContentEmailModel;
import com.example.SocialMediaApp.Notification.domain.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ContentNotificationService {

    private final EmailTemplateService emailTemplateService;
    private final EmailSendingProperties emailSendingProperties;
    private final WebClient webClient;

    @Value("${spring.application.name}")
    private String applicationName;

    public ContentNotificationService(EmailTemplateService emailTemplateService,EmailSendingProperties emailSendingProperties,@Qualifier("emailWebClient") WebClient webClient){
        this.emailTemplateService=emailTemplateService;
        this.emailSendingProperties=emailSendingProperties;
        this.webClient=webClient;
    }

    public void sendEmail(Email email){
        Map<String,String> sender=Map.of("email", emailSendingProperties.getSenderEmail(),"name",applicationName);
        String htmlContent=buildHtmlContent(email);
        List<Map<String,String>> to= List.of(Map.of("email",email.getTo()));
        EmailSendingRequest request=EmailSendingRequest.builder().
                                         sender(sender).to(to).scheduledAt(email.getAt()).
                                                htmlContent(htmlContent).subject(email.getSubject()).build();
        EmailSendingResponse response=webClient.post().uri("/smtp/email")
                .bodyValue(request).retrieve()
                .bodyToMono(EmailSendingResponse.class).block();

    }

    private String buildHtmlContent(Email email){
        String htmlContent=null;

        if(email instanceof ContentEmail contentEmail){
            ContentEmailModel emailModel=ContentEmailModel.builder().postId(contentEmail.getPostId()).scheduledAt(contentEmail.getAt()).build();
            htmlContent=emailTemplateService.buildPostPublishingHtml(emailModel);
        }

        return htmlContent;
    }

}

