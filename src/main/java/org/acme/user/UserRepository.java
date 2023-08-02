package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NamedQuery;


@ApplicationScoped

//@NamedQuery(name = "UserModel.getByFirstName", query = "SELECT DISTINCT firstName FROM UserModel WHERE UPPER(firstName) LIKE UPPER('%firstName%')")
public class UserRepository implements PanacheRepository<UserModel> {


}
