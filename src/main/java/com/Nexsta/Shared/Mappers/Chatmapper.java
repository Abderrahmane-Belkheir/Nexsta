package com.Nexsta.Shared.Mappers;

import com.Nexsta.Messaging.api.dto.ChatSummary;
import com.Nexsta.Messaging.api.dto.ChatUser;
import com.Nexsta.Messaging.api.dto.MessageDTO;
import com.Nexsta.Messaging.domain.Message;
import com.Nexsta.Profile.api.dto.ProfileSummary;
import com.Nexsta.Profile.domain.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Chatmapper {

    ChatUser tochatUser(Profile profile);
    MessageDTO tomessageDTO(Message message);
    ChatSummary toChatDTO(ProfileSummary profileSummary);
}
