package org.acme.security.refreshtoken;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.security.SecurityUtils;
import org.acme.user.UserModel;
import org.acme.user.UserRepository;

import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public RefreshTokenModel findByToken(String token){
        PanacheQuery<RefreshTokenModel> refreshToken = refreshTokenRepository.find("token", token);
        if(Objects.isNull(refreshToken.firstResult())){
            throw new ObjectNotFoundException("Token não encontrado");
        }
        return refreshToken.firstResult();


    }
    public RefreshTokenModel findByUser(UserModel userModel){
        PanacheQuery<RefreshTokenModel> refreshTokenByUser = refreshTokenRepository.find("userModel", userModel);

        if(Objects.isNull(refreshTokenByUser.firstResult())){
            throw new ObjectNotFoundException("Token não encontrado");
        }
        return refreshTokenByUser.firstResult();
    }
    public RefreshTokenModel findByUserNonNull(UserModel userModel){
        PanacheQuery<RefreshTokenModel> refreshTokenByUser = refreshTokenRepository.find("userModel", userModel);

        return refreshTokenByUser.firstResult();
    }

    @Transactional
    public void createRefreshToken(String email){
        PanacheQuery<UserModel> userByEmail = userRepository.find("email", email);

        if(Objects.nonNull(findByUserNonNull(userByEmail.firstResult()))){
            refreshTokenRepository.delete(findByUserNonNull(userByEmail.firstResult()));
            refreshTokenRepository.flush();
        }

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.setUserModel(userByEmail.firstResult());
        refreshTokenModel.setToken(UUID.randomUUID().toString());

       refreshTokenRepository.persist(refreshTokenModel);
    }

    @Transactional
    public void deleteToken(){
        String id = securityUtils.getIdfromDecodedCookie();
        UserModel userModel = userRepository.findById(Long.parseLong(id));
        RefreshTokenModel refreshTokenModel = findByUser(userModel);

        refreshTokenRepository.delete(refreshTokenModel);

    }
}
