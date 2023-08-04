package org.acme.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "jakarta",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserModel toUserModel(UserDto userDto);

    UserDto toUserDto(UserModel userModel);

    @Mapping(target = "id", ignore = true)
    void updateUser(UserDto userDto, @MappingTarget UserModel userModel);

}
