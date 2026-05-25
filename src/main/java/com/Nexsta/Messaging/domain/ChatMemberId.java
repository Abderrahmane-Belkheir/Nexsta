package com.Nexsta.Messaging.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class ChatMemberId implements Serializable {

    private String chatId;

    private String userId;

    public ChatMemberId() {}

    public ChatMemberId(String chatId, String userId) {
        this.chatId = chatId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMemberId that)) return false;

        return Objects.equals(chatId, that.chatId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, userId);
    }
}
