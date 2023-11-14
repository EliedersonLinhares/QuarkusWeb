package org.acme.utils;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.acme.user.UserModel;
import org.acme.user.UserService;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class RegistrationCompleteEvent {

    private final UserService userService;


    public void sendVerificationToken(String email,String applicationUrl) {
        //1. Get the new registered user
       UserModel user = userService.getUserByEmail(email);

        //2. Create a verification token for the user
        String verificationToken = UUID.randomUUID().toString();

        //3. save the verification token for the user
        userService.saveUserVerificationToken(user,verificationToken);

        //4 build the verification url to be sent to the user
        String url = applicationUrl + "/user/verifyEmail?token=" + verificationToken;
        //5. send the email
        log.info("Url example: {}", url);


    }





}
