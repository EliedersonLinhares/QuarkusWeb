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
   static final String UPDATE_ERROR = "Error updating user";

    public UserModel getUserById(long id){

        return userRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    @Transactional
    public void save2User(UserDto user) {
        verifyEmailForRegister(user);

            UserModel userModel = new UserModel();
            userMapper.setPasswordAndRole(user,userModel);
            userRepository.persistAndFlush(userMapper.toUserModel(user));

    }

    public UserModel getUserByEmail(String email) {

       return userRepository.find("email", email).firstResult();



    }

    private void verifyEmailForRegister(UserDto user) {

        if(Objects.nonNull(getUserByEmail(user.email()))){
            throw new ObjectNotFoundException("Email already in use");
        }
    }

    @Transactional
    public Map<String, Object> authenticate(LoginDto loginDto) {
        UserModel user = getUserByEmail(loginDto.email());

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
                .verify(loginDto.password().toCharArray(),
                user.getPassword()).verified){

            throw new ObjectNotFoundException("Credentials invalid");
        }


        refreshTokenService.createRefreshToken(loginDto.email());


        Set<String> roles2 = user.getRoles();
        return securityUtils.encryptJwt(user, roles2);

    }

    public Map<String, Object> newJWT(UserModel userModel){
            UserModel user = getUserByEmail(userModel.getEmail());
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
            throw new ObjectNotFoundException(UPDATE_ERROR);
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

    public Object findByEmail(String sort, String order, int page, int size, String email) {
        return getPaginatedResponse(userRepository.find(
                "lower(email) like :email",
                sortOrder(sort, order),
                Parameters.with("email", "%" + email.toLowerCase() + "%")), page, size);
    }

    public Object findByUsernameAndEmail(String sort, String order, int page, int size, String username,
            String email) {
        return getPaginatedResponse(userRepository.find(
                "lower(username) like :username and lower(email) like :email",
                sortOrder(sort, order),
                Parameters.with("username", "%" + username.toLowerCase() + "%")
                        .and("email", "%" + email.toLowerCase() + "%")),
                page, size);
    }

    private Sort sortOrder(String sort, String order) {
        return Sort.by(sort).direction(Sort.Direction.valueOf(order));
    }


@Transactional
    public void saveUserVerificationToken(UserModel user, String token) {
        int times = 1;
      var verificationToken  = new VerificationTokenModel(token,user,times);
     tokenRepository.persist(verificationToken);
    }

    @Transactional
    public String validateToken(String verificationToken) {

        VerificationTokenModel token = getVerificationToken(verificationToken);


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
    public void saveToken(VerificationTokenModel token){
        tokenRepository.persist(token);
    }
    @Transactional
    public void deleteToken(VerificationTokenModel token){
        tokenRepository.delete(token);
    }
    public VerificationTokenModel getVerificationToken(String verificationToken) {
        return tokenRepository.find("token", verificationToken).firstResult();
    }
    public VerificationTokenModel getVerificationTokenByUser(UserModel userModel) {
        return tokenRepository.find("user", userModel).firstResult();
    }

    @Transactional
    public VerificationTokenModel generateNewVerificationToken(String oldToken) {
        VerificationTokenModel verificationToken = getVerificationToken(oldToken);
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
