package com.Nexsta.Shared.Mappers;

import com.Nexsta.User.api.dto.UserRegistration;
import com.Nexsta.User.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Usermapper {

    User toUser(UserRegistration userregistration);
}
