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
import org.acme.security.refreshtoken.RefreshTokenService;
import org.acme.security.verificationtoken.VerificationTokenModel;
import org.acme.security.verificationtoken.VerificationTokenRepository;

import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository tokenRepository;

    public UserModel getUserById(long id){

        return userRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    @Transactional
    public void save2User(UserDto user) {
        verifyEmail(user);
        try {
          UserModel userModel = new UserModel();

            userMapper.setPasswordAndRole(user,userModel);
            userRepository.persistAndFlush(userMapper.toUserModel(user));
        }catch (RuntimeException e) {
            throw new ObjectNotFoundException("Error saving user");
        }

    }

    public UserModel getUserByEmail(String email) {
       return userRepository.find("email", email).firstResult();

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

        refreshTokenService.createRefreshToken(loginDto.email());


        Set<String> roles2 = userByEmail.firstResult().getRoles();
        return securityUtils.encryptJwt(userByEmail, roles2);
    }

    public Map<String, Object> newJWT(UserModel userModel){
        PanacheQuery<UserModel> userByEmail = userRepository.find("email", userModel.getEmail());
        if(Objects.isNull(userByEmail.firstResult())){
            throw new ObjectNotFoundException("User not found");
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

    public Object findByUsername(String sort, String order, int page, int size, String username) {
        return getPaginatedResponse(userRepository.find(
                "lower(username) like :username",
                sortOrder(sort, order),
                Parameters.with("username", "%" + username.toLowerCase() + "%")), page, size);
    }
/**
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
**/
    private Sort sortOrder(String sort, String order) {
        return Sort.by(sort).direction(Sort.Direction.valueOf(order));
    }



    public void saveUserVerificationToken(UserModel user, String token) {
        int times = 1;
    var verificationToken  = new VerificationTokenModel(token,user,times);
     tokenRepository.persist(verificationToken);
    }

    @Transactional
    public String validateToken(String verificationToken) {
        PanacheQuery<VerificationTokenModel> token = tokenRepository.find("token", verificationToken);
       if(token.stream().findFirst().isEmpty()){
           return "invalid";
       }

       if(token.firstResult().getCheckedTimes() > 4){

           return "abuse";
       }



        UserModel user = token.firstResult().getUser();
        Calendar calendar = Calendar.getInstance();
        if((token.firstResult().getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0){

            int times = token.firstResult().getCheckedTimes() + 1;
            token.firstResult().setCheckedTimes(times);
            tokenRepository.persistAndFlush(token.firstResult());
            return  "expired";
        }
        user.setChecked(true);
        userRepository.persist(user);
        tokenRepository.delete(token.firstResult());
        return "valid";
    }

    @Transactional
    public VerificationTokenModel generateNewVerificationToken(String oldToken) {
        VerificationTokenModel verificationToken = tokenRepository.find("token", oldToken).firstResult();
        var verificationTokenTime = new VerificationTokenModel();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpirationTime(verificationTokenTime.getTokenExpirationTime());
        tokenRepository.persist(verificationToken);
        return verificationToken;
    }
}
