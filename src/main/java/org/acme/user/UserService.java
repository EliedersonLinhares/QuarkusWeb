package org.acme.user;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.acme.exceptions.ObjectNotFoundException;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserModel getUserById(long id){
        return userRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));
    }

    @Transactional
    public void save2User(UserDto user) {
        try {
            userRepository.persist(userMapper.toUserModel(user));
        }catch (RuntimeException e) {
            throw new ObjectNotFoundException("Error saving user");
        }

    }

    @Transactional
    public void update2User(UserDto user, long id) {
          try {
              UserModel userModel = getUserById(id);
              userMapper.updateUser(user,userModel);
              userRepository.persist(userModel);

          }catch (RuntimeException e){
              throw new ObjectNotFoundException("Error updating user");
          }

    }

    @Transactional
    public void deleteUser(long id) {
        try{
            UserModel userModel = getUserById(id);
            userRepository.delete(userModel);
        }catch (RuntimeException e){
            throw new ObjectNotFoundException("Error deleting user");
        }

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
