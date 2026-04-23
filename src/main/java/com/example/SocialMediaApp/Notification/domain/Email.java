package com.example.SocialMediaApp.Notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
@Getter
@AllArgsConstructor
public abstract class Email {
    private List<Map<String, String>> to;
    private String subject;
    private String at;
}
