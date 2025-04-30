package ru.maverick.cardmanagementsystem.user.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.maverick.cardmanagementsystem.auth.request.*;
import ru.maverick.cardmanagementsystem.user.dto.UserDto;
import ru.maverick.cardmanagementsystem.user.model.User;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "request.email", target = "email")
    @Mapping(source = "request.password", target = "password")
    @Mapping(source = "request.role", target = "role")
    User toUser(RegistrationRequest request);

    @Mapping(source = "request.email", target = "email")
    @Mapping(source = "request.password", target = "password")
    User toUser(AuthenticationRequest request);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.role", target = "role")
    RegistrationResponse toRegistrationResponse(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    UserDto toDto(User user);
}