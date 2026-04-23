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

    public String buildPostPublishing(ContentEmailModel emailTemplate){
        Context context=new Context();
        context.setVariable("scheduledTime",emailTemplate.getScheduledAt());
        context.setVariable("redirectUrl",emailTemplate.getRedirectUrl());


        return templateEngine.process("emails/post-publish-notification",context);
    }

}
