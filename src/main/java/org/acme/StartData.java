package org.acme;


import at.favre.lib.crypto.bcrypt.BCrypt;
import io.quarkus.runtime.Startup;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.acme.user.UserModel;
import org.acme.user.UserRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.Set;


@RequiredArgsConstructor
public class StartData {

    private final UserRepository userRepository;

    @ConfigProperty(name = "admin.username")
    String username;
    @ConfigProperty(name = "admin.password")
    String password;

    @ConfigProperty(name = "admin.mail")
    String mail;

    @Transactional
    @Startup
   void startup(){
        if(userRepository.findAll().stream().findAny().isEmpty()) {
            UserModel adminUser = new UserModel();
            adminUser.setUsername(username);
            adminUser.setEmail(mail);
            adminUser.setPassword(BCrypt.withDefaults().hashToString(12,password.toCharArray()));
            Set<String> roleUser = new HashSet<>();
            roleUser.add("admin");
            roleUser.add("user");
            adminUser.setRoles(roleUser);
            userRepository.persist(adminUser);
        }
    }
    }


