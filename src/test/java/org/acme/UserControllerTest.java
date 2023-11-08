package org.acme;


import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    Map<String,String> cookie;



    @Order(1)
    @DisplayName("[POST] Save User")
    @Test
    void addUser(){

        JsonObject user = new JsonObject();
        user.put("username", "Jhon");
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "123456");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }

    @Order(2)
    @DisplayName("[GET] Login user with email and password")
    @Test
    void loginByEmail(){
        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "123456");

        cookie =  RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(2))
                .body("firstName", equalTo("Jhon"))
                .extract()
                .response()
                .getCookies();
    }







    @Order(3)
    @DisplayName("[GET] Login with Wrong Password")
    @Test
    void loginByEmailWithWrongPassword(){
        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "1234567");

         RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("error",equalTo("Credentials invalid"))
                .body("status",equalTo(400));
    }
    @Order(4)
    @DisplayName("[GET] Login with Wrong email")
    @Test
    void loginByEmailWithWrongEmail(){
        JsonObject user = new JsonObject();
        user.put("email", "jhon2.spencer@mail.com");
        user.put("password", "123456");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("error",equalTo("User not found"))
                .body("status",equalTo(400));
    }


    //@Disabled
    @Order(5)
    @DisplayName("[GET] Get User by id")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "user")
    void getUserById(){
        System.out.println(cookie);

       RestAssured.given()
               .contentType(MediaType.APPLICATION_JSON)
               .when()
               .cookies(cookie)
               .get("user/2")
               .then()
               .statusCode(Response.Status.OK.getStatusCode())
               .body("id", equalTo(2))
               .body("username", equalTo("Jhon"))
               .body("email",equalTo( "jhon.spencer@mail.com"))
               .body("roles", hasItem("user"));
//        io.restassured.response.Response res = RestAssured.given()
//                .when()
//                .cookies(cookie)
//                .get("user/1");
//
//        System.out.println(res.getBody().jsonPath().prettify());

    }


    @Order(7)
    @DisplayName("[UPDATE] Update role to admin")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void updateRole(){


        Set<String> roles = new HashSet<>();
        //roles.add("user");
        roles.add("admin");
        JsonObject user = new JsonObject();
        user.put("roles", roles);

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .cookies(cookie)
                .put("user/roles/2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie)
                .get("user/2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("roles", hasItem("admin"));
    }

    @Order(8)
    @DisplayName("[PUT] Update password")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "user")
    void updatePassword(){

        JsonObject user = new JsonObject();
        user.put("password", "1234567");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .cookies(cookie)
                .put("user/password/2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());


    }

    @Order(9)
    @DisplayName("[GET] Get other user information by user role")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "user")
    void GetOtherUserInformation(){
        JsonObject user = new JsonObject();
        user.put("username", "Catherine");
        user.put("email", "catherine.mary@mail.com");
        user.put("password", "123456");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());


        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie)
                .get("user/3")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("error",equalTo("User can return only your data"))
                .body("status",equalTo(400));
    }

    @Order(10)
    @DisplayName("[POST] Error when try use a email already in use")
    @Test
    void addUserWithEmailAlreadyTaken(){

        JsonObject user = new JsonObject();
        user.put("username", "Jhon");
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "123456");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("error",equalTo("Email already in use"))
                .body("status",equalTo(400));
    }



  //  @Disabled
    @Order(11)
    @DisplayName("[GET] Get All Users paginated")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void getUsersPaginate(){
        JsonObject user2 = new JsonObject();
        user2.put("username", "Serena");
        user2.put("email", "serena.maia@mail.com");
        user2.put("password", "123456");
        JsonObject user3 = new JsonObject();
        user3.put("username", "Myke");
        user3.put("email", "myke.ramos@mail.com");
        user3.put("password", "123456");
        JsonObject user4 = new JsonObject();
        user4.put("username", "karine");
        user4.put("email", "karine.silva@mail.com");
        user4.put("password", "123456");
        JsonObject user5 = new JsonObject();
        user5.put("username", "Alexandre");
        user5.put("email", "alexandre.ramalho@mail.com");
        user5.put("password", "123456");
        JsonObject user6 = new JsonObject();
        user6.put("username", "Michele");;
        user6.put("email", "michele.cerqueira@mail.com");
        user6.put("password", "123456");
        JsonObject user7 = new JsonObject();
        user7.put("username", "Bruno");
        user7.put("email", "bruno.alvez@mail.com");
        user7.put("password", "123456");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user2.toString())
                .when()
                .post("user");
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user3.toString())
                .when()
                .post("user");
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user4.toString())
                .when()
                .post("user");
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user5.toString())
                .when()
                .post("user");
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user6.toString())
                .when()
                .post("user");
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user7.toString())
                .when()
                .post("user");


        RestAssured.given()
                .when()
                .get("user/userpaginated")
                .then()
                .body("totalItems", equalTo(9))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(0))
                .statusCode(Response.Status.OK.getStatusCode());
    }
   // @Disabled
    @Order(12)
    @DisplayName("[GET] Get All Users paginated with page size changed to 10 ")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryPageSizeChanged(){
        RestAssured.given()
                .queryParam("size",10)
                .when()
                .get("user/userpaginated")
                .then()
                .body("totalItems", equalTo(9))
                .body("totalPages", equalTo(1))
                .body("currentPage", equalTo(0))
                .statusCode(Response.Status.OK.getStatusCode());
    }
  //  @Disabled
    @Order(13)
    @DisplayName("[GET] Get All Users paginated with actual page changed to second")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryActualPageChanged(){
        RestAssured.given()
                .queryParam("page",1)
                .when()
                .get("user/userpaginated")
                .then()
                .body("totalItems", equalTo(9))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(1))
                .statusCode(Response.Status.OK.getStatusCode());
    }
   // @Disabled
    @Order(14)
    @DisplayName("[GET] Get All Users paginated with sorted changed by firstName")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQuerySortChanged(){
     String json =   RestAssured.given()
                .queryParam("sort","username")
                .when()
                .get("user/userpaginated")
             .asString();

//        String completeResponse = JsonPath.read(json, "$").toString();
//        System.out.println("-----completeResponse---");
//        System.out.println(completeResponse);
//        String allFirstNames = JsonPath.read(json, "$..firstName").toString();
//        System.out.println("-----allFirstNames---");
//        System.out.println(allFirstNames);
        String firstArrayItem = JsonPath.read(json, "$.users[0].username").toString();
//        System.out.println("-----firstArrayItem---");
//        System.out.println(firstArrayItem);
        Assertions.assertTrue(firstArrayItem.contains("Alexandre"));

    }

  //  @Disabled
    @Order(15)
    @DisplayName("[GET] Get All Users paginated with order changed to descending")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryOrderChanged(){
        String json =   RestAssured.given()
                .queryParam("sort","username")
                .queryParam("order","Descending")
                .when()
                .get("user/userpaginated")
                .asString();

        String firstArrayItem = JsonPath.read(json, "$.users[0].username").toString();
        Assertions.assertTrue(firstArrayItem.contains("karine"));

    }
    //@Disabled
    @Order(16)
    @DisplayName("[GET] Get All Users paginated and filtered by firstName with letter 'm'")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryFilterFirstNameByPartialStringIgnoreCase(){
        String json =   RestAssured.given()
                .queryParam("username","m")
                .when()
                .get("user/userpaginated")
                .asString();

        String ArrayItem = JsonPath.read(json, "$..username").toString();

        Assertions.assertTrue(ArrayItem.contains("Myke"));
       Assertions.assertTrue(ArrayItem.contains("Michele"));
    }
    //@Disabled

    /**
    @Order(17)
    @DisplayName("[GET] Get All Users paginated and filtered by lastName with letter 'o'")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryFilterLastNameByPartialStringIgnoreCase(){
        String json = RestAssured.given()
                .queryParam("lastname","o")
                .when()
                .get("user/userpaginated")
                .asString();

        String ArrayItems = JsonPath.read(json, "$..lastName").toString();

        Assertions.assertTrue(ArrayItems.contains("Ramos"));
        Assertions.assertTrue(ArrayItems.contains("Ramalho"));

    }
    //@Disabled
    @Order(18)
    @DisplayName("[GET] Get All Users paginated and filtered by firstName with letter 'm' and lastName 'o'")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void userPaginatedQueryFilterLastNameAndLastNameByPartialStringIgnoreCase(){
        String json = RestAssured.given()
                .queryParam("firstname","m")
                .queryParam("lastname","o")
                .when()
                .get("user/userpaginated")
                .asString();
        String ArrayItems = JsonPath.read(json, "$..lastName").toString();
        Assertions.assertTrue(ArrayItems.contains("Ramos"));

    }
**/
   //@Disabled
    @Order(19)
    @DisplayName("[PUT] Update User")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "user")
    void updateUser(){
        JsonObject user = new JsonObject();
        user.put("username", "Jhon2");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .cookies(cookie)
                .put("user/2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());



        RestAssured.given()
                .when()
                .cookies(cookie)
                .get("user/2")
                .then()
                .body("username", equalTo("Jhon2"))
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Order(20)
    @DisplayName("[GET] Get user not exists")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "user")
    void GetOtherUserNotExist(){
        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie)
                .get("user/999")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("error",equalTo("User can return only your data"))
                .body("status",equalTo(400));
    }
    //@Disabled
    @Order(21)
    @DisplayName("[GET] logout")
    @Test
    void LogoutUser(){

        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "1234567");

        Map<String,String> cookie1 =  RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(2))
                .body("firstName", equalTo("Jhon2"))
                .extract()
                .response()
                .getCookies();

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie1)
                .get("user/logout")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }



   // @Disabled
    @Order(22)
    @DisplayName("[DELETE] delete user by id")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void deleteUserById(){

        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "1234567");

        Map<String,String> cookie1 =  RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(2))
                .body("firstName", equalTo("Jhon2"))
                .extract()
                .response()
                .getCookies();
 System.out.println(cookie1);

     RestAssured.given()
                .when()
                .cookies(cookie1)
                .delete("user/4")
             .then()
             .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    //   System.out.println(res);

        RestAssured.given()
                .when()
                .cookies(cookie1)
                .get("user/4")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Order(23)
    @DisplayName("[POST] refresh token")
    @Test
    @TestSecurity(user = "jhon.spencer@mail.com", roles = "admin")
    void RefreshToken() {

        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "1234567");

        Map<String, String> cookie1 = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(2))
                .body("firstName", equalTo("Jhon2"))
                .extract()
                .response()
                .getCookies();

        Map<String, String> cookie2 = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie1)
                .post("user/refreshtoken")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .response()
                .getCookies();

        assertNotEquals(cookie1, cookie2);

    }
    @Order(24)
    @DisplayName("[GET] retrieve logged user information")
    @Test
    void getUserInformation(){

        JsonObject user = new JsonObject();
        user.put("email", "jhon.spencer@mail.com");
        user.put("password", "1234567");

        Map<String, String> cookie1 = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user/login")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(2))
                .body("firstName", equalTo("Jhon2"))
                .extract()
                .response()
                .getCookies();

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .cookies(cookie1)
                .get("user/userinformation")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id",equalTo("2"))
                .body("username",equalTo("Jhon2"));
    }



}
