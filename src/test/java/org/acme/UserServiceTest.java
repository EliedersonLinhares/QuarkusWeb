package org.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.user.UserMapper;
import org.acme.user.UserModel;
import org.acme.user.UserService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Tag("Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Inject
    UserService userService;

    UserModel userModel;

    @Inject
    UserMapper userMapper;

    @BeforeAll
    void setUp() {
        UserModel userModel2 = new UserModel();
        userModel2.setFirstName("Marcia");
        userModel2.setLastName("Abrantes");
        userModel2.setEmail("marcia.abrantes@mail.com");
        userModel2.setGender("feminino");
        userModel2.setPassword("123456");
        userService.save2User(userMapper.toUserDto(userModel2));


        UserModel userModel3 = new UserModel();
        userModel3.setFirstName("Roberto");
        userModel3.setLastName("Nascimento");
        userModel3.setEmail("roberto.nascimento@mail.com");
        userModel3.setGender("masculino");
        userModel3.setPassword("123456");
        userService.save2User(userMapper.toUserDto(userModel3));

    }

    @Order(1)
    @DisplayName("Save User")
    @Test
    void saveUserTest() {
        //given
        UserModel userModel1 = new UserModel();
        userModel1.setFirstName("Eduardo");
        userModel1.setLastName("brandão");
        userModel1.setEmail("eduardo.brandao@mail.com");
        userModel1.setGender("masculino");
        userModel1.setPassword("123456");


        //when
        userService.save2User(userMapper.toUserDto(userModel1));
        userModel = userService.getUserById(3L);


        //then
        assertNotNull(userModel);
        assertTrue(userModel.getFirstName().contains("Eduardo"));
        assertTrue(userModel.getLastName().contains("brandão"));
        assertTrue(userModel.getEmail().contains("eduardo.brandao@mail.com"));
        assertTrue(userModel.getGender().contains("masculino"));


    }
    @Order(2)
    @DisplayName("Get All Users Sorted by id ASC")
    @Test
    void getAllUsersSortedByIDASC() throws JsonProcessingException {
        ObjectMapper Obj = new ObjectMapper();

        //when
        Object object = userService.findAllUsersSorted("id", "Ascending", 0, 5);

        String json = Obj.writeValueAsString(object);

        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirsUser = JsonPath.read(json, "$.users[0].firstName").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].firstName").toString();
        String ThirdUser = JsonPath.read(json, "$.users[2].firstName").toString();

       // System.out.println(ArrayItems);

        //then
        assertTrue(totalItems.contains("3"));
        assertTrue(FirsUser.contains("Marcia"));
        assertTrue(SecondUser.contains("Roberto"));
        assertTrue(ThirdUser.contains("Eduardo"));

    }
    @Order(3)
    @DisplayName("Get All Users Sorted by id DESC")
    @Test
    void getAllUsersSortedByIDDesc() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findAllUsersSorted("id", "Descending", 0, 5);

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirsUser = JsonPath.read(json, "$.users[0].firstName").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].firstName").toString();
        String ThirdUser = JsonPath.read(json, "$.users[2].firstName").toString();

        //then
        assertTrue(totalItems.contains("3"));
        assertTrue(FirsUser.contains("Eduardo"));
        assertTrue(SecondUser.contains("Roberto"));
        assertTrue(ThirdUser.contains("Marcia"));
    }
    @Order(4)
    @DisplayName("Get All Users sorted and filtered by firstName ASC")
    @Test
    void getAllUsersFilterByFirstNamePartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findByFirstName("firstName","Ascending",0,5,"a");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirsUser = JsonPath.read(json, "$.users[0].firstName").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].firstName").toString();

        //then
        assertTrue(totalItems.contains("2"));
       assertTrue(FirsUser.contains("Eduardo"));
       assertTrue(SecondUser.contains("Marcia"));
    }
    @Order(5)
    @DisplayName("Get All Users sorted and filtered by lastName ASC")
    @Test
    void getAllUsersFilterBylastNamePartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findBylastName("lastName","Ascending",0,5,"o");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirsUser = JsonPath.read(json, "$.users[0].lastName").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].lastName").toString();
      // System.out.println(result);
       System.out.println(SecondUser);
        //then
        assertTrue(totalItems.contains("2"));
        assertTrue(FirsUser.contains("Nascimento"));
       assertTrue(SecondUser.contains("brandão"));
    }
    @Order(6)
    @DisplayName("Get All Users sorted by id and filtered by firstName and lastName ASC")
    @Test
    void getAllUsersFilterByFirstNameAndlastNamePartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findByfirstNameAndlastName("id","Ascending",0,5,"a","o");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String user = JsonPath.read(json, "$.users[0]").toString();

        //then
       assertTrue(totalItems.contains("1"));
       assertTrue(user.contains("Eduardo"));
       assertTrue(user.contains("brandão"));
    }


    @Order(7)
    @DisplayName("Get User by id")
   @Test
    void getUserByIdTest() {
        //when
        userModel = userService.getUserById(1L);

        //then
        assertNotNull(userModel);
        assertTrue(userModel.getFirstName().contains("Marcia"));
        assertTrue(userModel.getLastName().contains("Abrantes"));
        assertTrue(userModel.getEmail().contains("marcia.abrantes@mail.com"));
        assertTrue(userModel.getGender().contains("feminino"));
    }

    @Order(8)
    @DisplayName("Update User")
    @Test
    void updateUsertest() {
        //given
        UserModel userEdit = new UserModel();
        userEdit.setFirstName("Marcia2");
        userEdit.setLastName("Abrantes2");
        userEdit.setGender("feminino2");


        //when
       userService.update2User(userMapper.toUpdateUserDto(userEdit), 1L);
        userModel = userService.getUserById(1L);
       //then
        assertTrue(userModel.getFirstName().contains("Marcia2"));
        assertTrue(userModel.getLastName().contains("Abrantes2"));
        assertTrue(userModel.getGender().contains("feminino2"));

    }

    @Order(9)
    @DisplayName("Delete USer")
    @Test
    void deleteUserTest() {

        //given
        userService.deleteUser(1L);

        //when
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(1L));
        String expectedMessage = "User not found";
        String actualMessage = exception.getMessage();

        //then
        assertTrue(actualMessage.contains(expectedMessage));

    }
    @Order(10)
    @DisplayName("Get User by id not found")
    @Test
    void getUserByIdThrowErrorTest() {


        //when
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () -> userService.getUserById(8L));
        String expectedMessage = "User not found";
        String actualMessage = exception.getMessage();

        //then
        assertTrue(actualMessage.contains(expectedMessage));
    }


}
