package org.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.exceptions.ObjectNotFoundException;
import org.acme.security.verificationtoken.VerificationTokenModel;
import org.acme.user.LoginDto;
import org.acme.user.UserMapper;
import org.acme.user.UserModel;
import org.acme.user.UserService;
import org.acme.utils.RegistrationCompleteEvent;
import org.junit.jupiter.api.*;

import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Tag("Service")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    RegistrationCompleteEvent event;

    UserModel userModel;

    @Inject
    UserMapper userMapper;


    @BeforeAll
    void setUp() {
        UserModel userModel2 = new UserModel();
        userModel2.setUsername("Marcia");
        userModel2.setEmail("marcia.abrantes@mail.com");
        userModel2.setPassword("123456");
        userService.save2User(userMapper.toUserDto(userModel2));

        UserModel userModel3 = new UserModel();
        userModel3.setUsername("Roberto");
        userModel3.setEmail("roberto.nascimento@mail.com");
        userModel3.setPassword("123456");
        userService.save2User(userMapper.toUserDto(userModel3));



    }



    @Order(1)
    @DisplayName("Save User")
    @Test
    void saveUserTest() {
        //given
        UserModel userModel1 = new UserModel();
        userModel1.setUsername("Eduardo");
        userModel1.setEmail("eduardo.brandao@mail.com");
        userModel1.setPassword("123456");


        //when
        userService.save2User(userMapper.toUserDto(userModel1));
        userModel = userService.getUserById(4L);


        //then
        assertNotNull(userModel);
        assertTrue(userModel.getUsername().contains("Eduardo"));
        assertTrue(userModel.getEmail().contains("eduardo.brandao@mail.com"));
    }

    @Order(2)
    @DisplayName("Save User error: Email Already Exists")
    @Test
    void saveUserTestEmailAlreadyInUse() {
        //given
        UserModel userModel1 = new UserModel();
        userModel1.setUsername("Eduardo");
        userModel1.setEmail("eduardo.brandao@mail.com");
        userModel1.setPassword("123456");


        //when
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.save2User(userMapper.toUserDto(userModel1)));
        String expectedMessage = "Email already in use";
        String actualMessage = exception.getMessage();

        //then
        assertTrue(actualMessage.contains(expectedMessage));

    }



    @Order(3)
    @DisplayName("Get All Users Sorted by id ASC")
    @Test
    void getAllUsersSortedByIDASC() throws JsonProcessingException {
        ObjectMapper Obj = new ObjectMapper();

        //when
        Object object = userService.findAllUsersSorted("id", "Ascending", 0, 5);

        String json = Obj.writeValueAsString(object);

        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirstUser = JsonPath.read(json, "$.users[0].username").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].username").toString();
        String ThirdUser = JsonPath.read(json, "$.users[2].username").toString();
        String ForthdUser = JsonPath.read(json, "$.users[3].username").toString();

        System.out.println(Stream.of(FirstUser,SecondUser,ThirdUser,ForthdUser).toList().toString());

        //then
        assertTrue(totalItems.contains("4"));
        assertTrue(FirstUser.contains("admin"));
        assertTrue(SecondUser.contains("Marcia"));
        assertTrue(ThirdUser.contains("Roberto"));
        assertTrue(ForthdUser.contains("Eduardo"));

    }

    @Order(4)
    @DisplayName("Get All Users Sorted by id DESC")
    @Test
    void getAllUsersSortedByIDDesc() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findAllUsersSorted("id", "Descending", 0, 5);

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirstUser = JsonPath.read(json, "$.users[0].username").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].username").toString();
        String ThirdUser = JsonPath.read(json, "$.users[2].username").toString();
        String ForthdUser = JsonPath.read(json, "$.users[3].username").toString();

        System.out.println(Stream.of(FirstUser,SecondUser,ThirdUser,ForthdUser).toList().toString());

        //then
        assertTrue(totalItems.contains("4"));
        assertTrue(FirstUser.contains("Eduardo"));
        assertTrue(SecondUser.contains("Roberto"));
        assertTrue(ThirdUser.contains("Marcia"));
        assertTrue(ForthdUser.contains("admin"));
    }

    @Order(5)
    @DisplayName("Get All Users sorted and filtered by username ASC")
    @Test
    void getAllUsersFilterByFirstNamePartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findByUsername("username","Ascending",0,5,"a");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirstUser = JsonPath.read(json, "$.users[0].username").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].username").toString();
        String ThirdUser = JsonPath.read(json, "$.users[2].username").toString();

        System.out.println(Stream.of(FirstUser,SecondUser,ThirdUser).toList().toString());
        //then
        assertTrue(totalItems.contains("3"));
        assertTrue(FirstUser.contains("Eduardo"));
       assertTrue(SecondUser.contains("Marcia"));
       assertTrue(ThirdUser.contains("admin"));
    }


    @Order(6)
    @DisplayName("Get All Users sorted and filtered by email ASC")
    @Test
    void getAllUsersFilterByEmailPartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findByEmail("email","Ascending",0,5,"bra");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String FirstUser = JsonPath.read(json, "$.users[0].email").toString();
        String SecondUser = JsonPath.read(json, "$.users[1].email").toString();
       System.out.println(totalItems);
       System.out.println(SecondUser);
        System.out.println(FirstUser);
        //then
        assertTrue(totalItems.contains("2"));
        assertTrue(FirstUser.contains("eduardo.brandao"));
       assertTrue(SecondUser.contains("marcia.abrantes"));
    }

    @Order(7)
    @DisplayName("Get All Users sorted by id and filtered by username and email ASC")
    @Test
    void getAllUsersFilterByUsernameAndEmailPartial() throws JsonProcessingException {
        //given
        ObjectMapper Obj = new ObjectMapper();
        Object object = userService.findByUsernameAndEmail("id","Ascending",0,5,"e","bra");

        //when
        String json = Obj.writeValueAsString(object);
        String totalItems = JsonPath.read(json, "$.totalItems").toString();
        String user = JsonPath.read(json, "$.users[0]").toString();
        System.out.println(totalItems);
        System.out.println(user);
        //then
       assertTrue(totalItems.contains("1"));
       assertTrue(user.contains("eduardo.brandao"));
    }


    @Order(7)
    @DisplayName("Get User by id")
   @Test
    void getUserByIdTest() {
        //when
        userModel = userService.getUserById(2L);

        //then
        assertNotNull(userModel);
        assertTrue(userModel.getUsername().contains("Marcia"));
        assertTrue(userModel.getEmail().contains("marcia.abrantes@mail.com"));
    }


    @Order(8)
    @DisplayName("Update User")
    @Test
    void updateUsertest() {
        //given
        UserModel userEdit = new UserModel();
        userEdit.setUsername("Marcia2");
        userEdit.setEnabled(false);


        //when
       userService.update2User(userMapper.toUpdateUserDto(userEdit), 1L);
        userModel = userService.getUserById(1L);
       //then
        assertTrue(userModel.getUsername().contains("Marcia2"));

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


    @Order(11)
    @DisplayName("Confirm user verification")
    @Test
    void ConfirmUserVerification(){

        //given
        UserModel user = userService.getUserByEmail("roberto.nascimento@mail.com");
        event.sendVerificationToken(user.getEmail(), "http://localhost:8080");
        VerificationTokenModel verificationToken = userService.getVerificationTokenByUser(user);

       //When
        String result = userService.validateToken(verificationToken.getToken());
        String expectedMessage = "valid";
        System.out.println(result);

       //Then
       assertTrue(result.contains(expectedMessage));
    }
    @Order(12)
    @DisplayName("Confirm user verification error: token invalid")
    @Test
    void ConfirmUserVerificationInvalid(){

        //given
        String Token = "1111-1111-11111";

        String result = userService.validateToken(Token);
        String expectedMessage = "invalid";
        System.out.println(result);

        //Then
        assertTrue(result.contains(expectedMessage));
    }
    @Order(13)
    @DisplayName("Confirm user verification error: multiple tries")
    @Test
    void ConfirmUserVerificationManyTries(){

        //given
        UserModel user = userService.getUserByEmail("roberto.nascimento@mail.com");
        String tokenUID = UUID.randomUUID().toString();


       VerificationTokenModel verificationToken = new VerificationTokenModel();
       verificationToken.setUser(user);
       verificationToken.setToken(tokenUID);
       verificationToken.setCheckedTimes(5);//token > 4
       verificationToken.setTimeLimit(userService.getTokenTimeoutTime());
       userService.saveToken(verificationToken);

       VerificationTokenModel verificationToken2 = userService.getVerificationTokenByUser(user);

        //When
        String result = userService.validateToken(verificationToken2.getToken());
        String expectedMessage = "abuse";
        System.out.println(result);

        //Then
        assertTrue(result.contains(expectedMessage));
    }

    @Order(14)
    @DisplayName("Confirm user verification error: expired")
    @Test
    void ConfirmUserVerificationExpired(){

        //given
        UserModel user = userService.getUserByEmail("roberto.nascimento@mail.com");
        VerificationTokenModel verificationToken2 = userService.getVerificationTokenByUser(user);
         if(Objects.nonNull(verificationToken2)){
             userService.deleteToken(verificationToken2);//delete previous token
         }


        String tokenUID = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();


        VerificationTokenModel verificationToken = new VerificationTokenModel();
        verificationToken.setUser(user);
        verificationToken.setToken(tokenUID);
        verificationToken.setCheckedTimes(0);
        verificationToken.setExpirationTime(calendar.getTime());//set actual time to expiration time

        userService.saveToken(verificationToken);

        VerificationTokenModel verificationToken3 = userService.getVerificationTokenByUser(user);

        //When
        String result = userService.validateToken(verificationToken3.getToken());
        String expectedMessage = "expired";
        System.out.println(result);

        //Then
        assertTrue(result.contains(expectedMessage));
    }
    @Order(15)
    @DisplayName("Confirm user verification error: time limit for many tries exceeded")
    @Test
    void ConfirmUserVerificationTimeLimit(){

        //given
        UserModel user = userService.getUserByEmail("roberto.nascimento@mail.com");
        VerificationTokenModel verificationToken2 = userService.getVerificationTokenByUser(user);
        if(Objects.nonNull(verificationToken2)){
            userService.deleteToken(verificationToken2);//delete previous token
        }


        String tokenUID = UUID.randomUUID().toString();
        Calendar calendar = Calendar.getInstance();


        VerificationTokenModel verificationToken = new VerificationTokenModel();
        verificationToken.setUser(user);
        verificationToken.setToken(tokenUID);
        verificationToken.setCheckedTimes(0);
        verificationToken.setExpirationTime(calendar.getTime());//actual time
        verificationToken.setTimeLimit(userService.getTokenTimeoutTime());// with time limit
        userService.saveToken(verificationToken);

        VerificationTokenModel verificationToken3 = userService.getVerificationTokenByUser(user);

        //When
        String result = userService.validateToken(verificationToken3.getToken());
        String expectedMessage = "timeout";
        System.out.println(result);

        //Then
        assertTrue(result.contains(expectedMessage));
    }
    @Order(16)
    @DisplayName("Authenticate user without check")
    @Test
    void AuthenticateUserWithoutCheck() {
        LoginDto loginDto = new LoginDto("marcia.abrantes@mail.com","123456");

        //when
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.authenticate(loginDto));
        String expectedMessage = "Account not checked, verify your email";
        String actualMessage = exception.getMessage();

        //then
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Order(17)
    @DisplayName("Authenticate User")
    @Test
    void AuthenticateUserSuccess(){

        //given
        UserModel user = userService.getUserByEmail("roberto.nascimento@mail.com");
        VerificationTokenModel verificationToken2 = userService.getVerificationTokenByUser(user);
        if(Objects.nonNull(verificationToken2)){
            userService.deleteToken(verificationToken2);//delete previous token
        }

        event.sendVerificationToken(user.getEmail(), "http://localhost:8080");
        VerificationTokenModel verificationToken = userService.getVerificationTokenByUser(user);

        //When
        String result = userService.validateToken(verificationToken.getToken());

        LoginDto loginDto = new LoginDto("roberto.nascimento@mail.com","123456");
        Map<String,Object> response =  userService.authenticate( loginDto);

        //Extract username from response
        String principal = response.get("user").toString();
        String firstName = principal.substring(principal.indexOf("firstName",principal.indexOf("=")) +12,principal.indexOf(",",principal.indexOf(",")));
        String expectedMessage = "valid";


        //Then
       assertTrue(result.contains(expectedMessage));
       assertTrue(firstName.contains(user.getUsername()));

    }

    @Order(18)
    @DisplayName("Authenticate user disabled")
    @Test
    void AuthenticateUserDisabled() {

        UserModel userModel4 = new UserModel();
        userModel4.setUsername("Felipe");
        userModel4.setEmail("felipe.azevedo@mail.com");
        userModel4.setPassword("123456");
        userModel4.setEnabled(false);
        userService.save2User(userMapper.toUserDto(userModel4));

        //given
        UserModel user = userService.getUserByEmail("felipe.azevedo@mail.com");
        VerificationTokenModel verificationToken2 = userService.getVerificationTokenByUser(user);
        if(Objects.nonNull(verificationToken2)){
            userService.deleteToken(verificationToken2);//delete previous token
        }

        event.sendVerificationToken(user.getEmail(), "http://localhost:8080");
        VerificationTokenModel verificationToken = userService.getVerificationTokenByUser(user);

        //When
        String result = userService.validateToken(verificationToken.getToken());


        LoginDto loginDto = new LoginDto("felipe.azevedo@mail.com","123456");

        //when
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> userService.authenticate(loginDto));
        String expectedMessage = "Disabled account, contact the Admin";
        String actualMessage = exception.getMessage();
   System.out.println(actualMessage);
        //then
        assertTrue(actualMessage.contains(expectedMessage));
    }

}
