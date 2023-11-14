package org.acme.security.verificationtoken;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VerificationTokenRepository implements PanacheRepository<VerificationTokenModel> {
}
