package org.acme.security.verificationtoken;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.acme.user.UserModel;

import java.util.Calendar;
import java.util.Date;

@Getter
@Setter
@Entity(name = "VerificationToken")
@NoArgsConstructor
public class VerificationTokenModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String token;
    private Date expirationTime;
    private Integer checkedTimes;

    private static final int EXPIRATION_TIME = 1;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    public VerificationTokenModel(String token, UserModel user, Integer checkedTimes) {
        super();
        this.token = token;
        this.user = user;
        this.checkedTimes = checkedTimes;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public VerificationTokenModel(String token) {
        super();
        this.token = token;
        this.expirationTime = this.getTokenExpirationTime();
    }

    public  Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);
        return new Date(calendar.getTime().getTime());
    }
}
