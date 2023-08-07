package org.acme.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.security.SecurityUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {



    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    public UserModel getUserById(long id){
        UserModel userModel = userRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
        //For the user only retrieve your data, only admins can get all users by id
        securityUtils.decodeJwtDataUserFromCookie(id);
        return userModel;
    }



    @Transactional
    public void save2User(UserDto user) {
        verifyEmail(user);
        try {
          UserModel userModel = new UserModel();
            userMapper.setPasswordAndRole(user,userModel);
            userRepository.persist(userMapper.toUserModel(user));
        }catch (RuntimeException e) {
            throw new ObjectNotFoundException("Error saving user");
        }

    }

    private void verifyEmail(UserDto user) {
        PanacheQuery<UserModel> userByEmail = userRepository.find("email", user.email());

        if(Objects.nonNull(userByEmail.firstResult())){
            throw new ObjectNotFoundException("Email already in use");
        }
    }

    public Map<String, Object> authenticate(LoginDto loginDto){
        PanacheQuery<UserModel> userByEmail = userRepository.find("email", loginDto.email());
        if(Objects.isNull(userByEmail.firstResult())){
            throw new ObjectNotFoundException("User not found");
        }
        if(!BCrypt.verifyer()
                .verify(loginDto.password().toCharArray(),
                userByEmail.firstResult().getPassword()).verified){
            throw new ObjectNotFoundException("Credentials invalid");
        }

        Set<String> roles2 = userByEmail.firstResult().getRoles();
        return securityUtils.encryptJwt(userByEmail, roles2);
    }



    public void updateUserRole(UpdateUserRole user, long id){
        UserModel userModel = getUserById(id);

        userMapper.updateUserRole(user, userModel);
        userRepository.persist(userModel);
    }

    public void updateUserPassword(UpdatePassword user, long id){
        UserModel userModel = getUserById(id);

        userMapper.encryptUserPasswordUpdate(user,userModel);
        userMapper.updatePassword(user, userModel);
        userRepository.persist(userModel);

    }

    @Transactional
    public void update2User(UpdateUserDto user, long id) {
        try{
              UserModel userModel = getUserById(id);
              userMapper.updateUser(user,userModel);
              userRepository.persist(userModel);
        }catch (RuntimeException e){
            throw new ObjectNotFoundException("Error deleting user");
        }

    }

    @Transactional
    public void deleteUser(long id) {
        try{
            UserModel userModel = getUserById(id);
            userRepository.delete(userModel);
        }catch (RuntimeException e){
            throw new ObjectNotFoundException("Error deleting user");
        }

    }

    public Map<String, Object> getPaginatedResponse(PanacheQuery<UserModel> userRepository, int page, int size) {
        userRepository.page(Page.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("users", userRepository.stream().toList());
        response.put("currentPage", page);
        response.put("totalItems", userRepository.count());
        response.put("totalPages", userRepository.pageCount());

        return response;
    }

    public Object findAllUsersSorted(String sort, String order, int page, int size) {
        return getPaginatedResponse(userRepository.findAll(Sort.by(sort)
                .direction(Sort.Direction.valueOf(order))), page, size);
    }

    public Object findByFirstName(String sort, String order, int page, int size, String firstName) {
        return getPaginatedResponse(userRepository.find(
                "lower(firstName) like :firstName",
                sortOrder(sort, order),
                Parameters.with("firstName", "%" + firstName.toLowerCase() + "%")), page, size);
    }

    public Object findBylastName(String sort, String order, int page, int size, String lastName) {
        return getPaginatedResponse(userRepository.find(
                "lower(lastName) like :lastName",
                sortOrder(sort, order),
                Parameters.with("lastName", "%" + lastName.toLowerCase() + "%")), page, size);
    }

    public Object findByfirstNameAndlastName(String sort, String order, int page, int size, String firstName,
            String lastName) {
        return getPaginatedResponse(userRepository.find(
                "lower(firstName) like :firstName and lower(lastName) like :lastName",
                sortOrder(sort, order),
                Parameters.with("firstName", "%" + firstName.toLowerCase() + "%")
                        .and("lastName", "%" + lastName.toLowerCase() + "%")),
                page, size);
    }

    private Sort sortOrder(String sort, String order) {
        return Sort.by(sort).direction(Sort.Direction.valueOf(order));
    }

    public String getDateTimeInCookieFormat() {
        OffsetDateTime oneHourFromNow
                = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(Duration.ofDays(7));
        return DateTimeFormatter.RFC_1123_DATE_TIME
                .format(oneHourFromNow);
    }
}
