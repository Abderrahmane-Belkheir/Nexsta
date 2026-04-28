package com.Nexsta.Notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
@Getter
@AllArgsConstructor
public abstract class Email {
    private String to;
    private String subject;
    private String at;
}
