package org.acme.user;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.security.SecurityUtils;
import org.acme.security.refreshtoken.RefreshTokenService;
import org.acme.security.verificationtoken.VerificationTokenModel;
import org.acme.security.verificationtoken.VerificationTokenRepository;

import java.util.*;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository tokenRepository;

    private static final int EXPIRATION_TIMEOUT = 4;

    public UserModel getUserById(long id){

        return userRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    @Transactional
    public void save2User(UserDto user) {
        verifyEmailForLogin(user);
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

    private void verifyEmailForLogin(UserDto user) {
        if(Objects.nonNull(getUserByEmail(user.email()))){
            throw new ObjectNotFoundException("Email already in use");
        }
    }

    @Transactional
    public Map<String, Object> authenticate(UserModel userModel) {

        UserModel user = getUserByEmail(userModel.getEmail());


        if(Objects.isNull(user)){
            throw new ObjectNotFoundException("User not found");
        }
        if(!user.isChecked()){
            throw new ObjectNotFoundException("Account not checked, verify your email");
        }
        if(!user.isEnabled()){
            throw new ObjectNotFoundException("Disabled account, contact the Admin");
        }

        if(!BCrypt.verifyer()
                .verify(userModel.getPassword().toCharArray(),
                user.getPassword()).verified){

            throw new ObjectNotFoundException("Credentials invalid");
        }


        refreshTokenService.createRefreshToken(userModel.getEmail());


        Set<String> roles2 = user.getRoles();
        return securityUtils.encryptJwt(user, roles2);
    }

    public Map<String, Object> newJWT(UserModel userModel){
        UserModel user = getUserByEmail(userModel.getEmail());
        if(Objects.isNull(user)){
            throw new ObjectNotFoundException("User not found");
        }

        Set<String> roles2 = user.getRoles();
        return securityUtils.encryptJwt(user, roles2);
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
            throw new ObjectNotFoundException("Error updating user");
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

        VerificationTokenModel token = tokenRepository.find("token", verificationToken).firstResult();


       if(Objects.isNull(token)){
           return "invalid";
       }
        UserModel user = token.getUser();

       if(token.getCheckedTimes() > 4){
           token.setCheckedTimes(0);
           token.setTimeLimit(getTokenTimeoutTime());
           tokenRepository.persistAndFlush(token);

          return "abuse";
       }
        Calendar calendar = Calendar.getInstance();

       if(Objects.nonNull(token.getTimeLimit()) && ((token.getTimeLimit().getTime() - calendar.getTime().getTime()) >= 0)) {
               return "timeout";
       }

        if((token.getExpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
            int times = token.getCheckedTimes() + 1;
            token.setCheckedTimes(times);
            tokenRepository.persistAndFlush(token);
            return  "expired";
        }

        user.setChecked(true);
        userRepository.persist(user);
        tokenRepository.delete(token);
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

    public  Date getTokenTimeoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, EXPIRATION_TIMEOUT);
        return new Date(calendar.getTime().getTime());
    }
}
