package org.acme.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    Long id;
    String username;
    String email;


    boolean isEnabled = true;
    boolean isChecked = false;


    @JsonIgnore
    String password;

    @ElementCollection
    @CollectionTable(name = "USER_ROLES")
    @Column(name = "roles")
    private Set<String> roles = new HashSet<>();

}
