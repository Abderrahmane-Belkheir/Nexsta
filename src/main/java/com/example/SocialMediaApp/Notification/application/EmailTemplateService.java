package com.example.SocialMediaApp.Notification.application;

import com.example.SocialMediaApp.Notification.domain.ContentEmailModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;



@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final SpringTemplateEngine templateEngine;

    public String buildPostPublishingHtml(ContentEmailModel emailTemplate){
        Context context=new Context();
        context.setVariable("scheduledTime",emailTemplate.getScheduledAt());
        context.setVariable("redirectUrl",buildPostPublishUrl(emailTemplate.getPostId()));

        return templateEngine.process("emails/post-publish-notification",context);
    }

    private String buildPostPublishUrl(String postId){
        return "http://localhost:8080/edit-post.html?postId="+postId;
    }

}
