package com.Nexsta.Notification.Configurations;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix ="brevo" )
@Component
@Setter
 public class EmailSendingProperties {

    private String url;
    private String apiKey;

    @Getter
    private String emailSender;

    protected String getUrl(){
        return url;
    }

    protected String getApiKey(){
        return apiKey;
    }

}
