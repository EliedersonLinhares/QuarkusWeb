package org.acme.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "jakarta",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    UserModel toUserModel(UserDto userDto);

    UserDto toUserDto(UserModel userModel);

    UpdateUserDto toUpdateUserDto(UserModel userModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateUser(UpdateUserDto updateUserDto, @MappingTarget UserModel userModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserRole(UpdateUserRole updateUserRole, @MappingTarget UserModel userModel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updatePassword(UpdatePassword updatePassword, @MappingTarget UserModel userModel);

    @AfterMapping
    default void setPasswordAndRole(UserDto userDto, @MappingTarget UserModel userModel){
        userModel.setPassword(BCrypt.withDefaults().hashToString(12,userDto.password().toCharArray()));
        Set<String> roleUser = new HashSet<>();
        roleUser.add("user");
        userModel.setRoles(roleUser);
    }
    @AfterMapping
    default void encryptUserPasswordUpdate(UpdatePassword updatePassword, @MappingTarget UserModel userModel){
        userModel.setPassword(BCrypt.withDefaults().hashToString(12,updatePassword.password().toCharArray()));
    }

}
