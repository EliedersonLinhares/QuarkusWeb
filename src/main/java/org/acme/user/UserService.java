package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserModel getUserById(long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public String save2User(UserDto user) {
        userRepository.persist(userMapper.toUserModel(user));
        return "saved";
    }

    @Transactional
    public String update2User(UserDto user, long id) {
        Optional<UserModel> optionalUserModel = userRepository.findByIdOptional(id);
        if (optionalUserModel.isPresent()) {
          userMapper.updateUser(user,optionalUserModel.get());
          userRepository.persist(optionalUserModel.get());
          return "updated";
        }

        return "error";
    }

    @Transactional
    public boolean deleteUser(long id) {
        return userRepository.deleteById(id);
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

    public Object findByFirstName(String sort, String order, int page, int size, String firstName) {
        return getPaginatedResponse(userRepository.find(
                "lower(firstName) like :firstName",
                sortOrder(sort, order),
                Parameters.with("firstName", "%" + firstName.toLowerCase() + "%")), page, size);
    }

    public Object findBylastName(String sort, String order, int page, int size, String lastName) {
        return getPaginatedResponse(userRepository.find(
                "lower(lastName) like :lastName",
                sortOrder(sort, order),
                Parameters.with("lastName", "%" + lastName.toLowerCase() + "%")), page, size);
    }

    public Object findByfirstNameAndlastName(String sort, String order, int page, int size, String firstName,
            String lastName) {
        return getPaginatedResponse(userRepository.find(
                "lower(firstName) like :firstName and lower(lastName) like :lastName",
                sortOrder(sort, order),
                Parameters.with("firstName", "%" + firstName.toLowerCase() + "%")
                        .and("lastName", "%" + lastName.toLowerCase() + "%")),
                page, size);
    }

    private Sort sortOrder(String sort, String order) {
        return Sort.by(sort).direction(Sort.Direction.valueOf(order));
    }

}
