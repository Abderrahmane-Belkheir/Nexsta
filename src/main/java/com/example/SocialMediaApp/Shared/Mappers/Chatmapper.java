package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Messaging.api.dto.ChatSummary;
import com.example.SocialMediaApp.Messaging.api.dto.ChatUser;
import com.example.SocialMediaApp.Messaging.api.dto.MessageDTO;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Profile.domain.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Chatmapper {

    ChatUser tochatUser(Profile profile);
    MessageDTO tomessageDTO(Message message);
    ChatSummary toChatDTO(ProfileSummary profileSummary);
}
