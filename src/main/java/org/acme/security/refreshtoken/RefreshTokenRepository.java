package org.acme.security.refreshtoken;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepository<RefreshTokenModel> {



}
