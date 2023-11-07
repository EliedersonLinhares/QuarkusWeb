package org.acme.security.refreshtoken;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.user.UserModel;

@Entity(name = "RefreshToken")
@Data
@NoArgsConstructor
public class RefreshTokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserModel userModel;

    @Column(nullable = false)
    private String token;

}
