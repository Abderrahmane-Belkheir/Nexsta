package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.Messaging.api.dto.ChatSummary;
import com.example.SocialMediaApp.Messaging.api.dto.ChatUser;
import com.example.SocialMediaApp.Messaging.api.dto.MessageDTO;
import com.example.SocialMediaApp.Messaging.domain.Message;
import com.example.SocialMediaApp.Profile.api.dto.ProfileSummary;
import com.example.SocialMediaApp.Profile.domain.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Chatmapper {



    @Mapping(target = "avatarurl", source = "publicavatarurl")
    ChatUser tochatUser(Profile profile);
    @Mapping(target = "messageId", source = "id")
    MessageDTO tomessageDTO(Message message);
    ChatSummary tochatDTO(ProfileSummary profileSummary);
}
