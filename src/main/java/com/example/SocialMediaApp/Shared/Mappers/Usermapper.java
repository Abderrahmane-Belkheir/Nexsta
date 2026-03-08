package com.example.SocialMediaApp.Shared.Mappers;

import com.example.SocialMediaApp.User.api.dto.UserRegistration;
import com.example.SocialMediaApp.User.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Usermapper {

    User toUserentity(UserRegistration userregistration);
}
