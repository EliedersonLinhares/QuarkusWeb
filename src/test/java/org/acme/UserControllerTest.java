package org.acme;


import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Order(1)
    @DisplayName("[POST] Save User")
    @Test
    void addUser(){

        JsonObject user = new JsonObject();
        user.put("id", 1L);
        user.put("firstName", "John");
        user.put("lastName", "Spencer");
        user.put("email", "jhon.spencer@mail.com");
        user.put("gender", "masculino");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .post("user")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());
    }
    @Order(2)
    @DisplayName("[GET] Get User by id")
    @Test
    void getUserById(){
        RestAssured.given()
                .when()
                .get("user/1")
                .then()
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Spencer"))
                .body("email", equalTo("jhon.spencer@mail.com"))
                .body("gender", equalTo("masculino"))
                .statusCode(Response.Status.OK.getStatusCode());
    }
    @Order(3)
    @DisplayName("[GET] Get All Users paginated")
    @Test
    void getUsersPaginate(){
        JsonObject user2 = new JsonObject();
        user2.put("id", 2L);
        user2.put("firstName", "Serena");
        user2.put("lastName", "Maya");
        user2.put("email", "serena.maia@mail.com");
        user2.put("gender", "feminino");
        JsonObject user3 = new JsonObject();
        user3.put("id", 3L);
        user3.put("firstName", "Myke");
        user3.put("lastName", "Ramos");
        user3.put("email", "myke.ramos@mail.com");
        user3.put("gender", "masculino");
        JsonObject user4 = new JsonObject();
        user4.put("id", 4L);
        user4.put("firstName", "karine");
        user4.put("lastName", "Silva");
        user4.put("email", "karine.silva@mail.com");
        user4.put("gender", "feminino");
        JsonObject user5 = new JsonObject();
        user5.put("id", 5L);
        user5.put("firstName", "Alexandre");
        user5.put("lastName", "Ramalho");
        user5.put("email", "alexandre.ramalho@mail.com");
        user5.put("gender", "masculino");
        JsonObject user6 = new JsonObject();
        user6.put("id", 6L);
        user6.put("firstName", "Michele");
        user6.put("lastName", "Cerqueira");
        user6.put("email", "michele.cerqueira@mail.com");
        user6.put("gender", "feminino");
        JsonObject user7 = new JsonObject();
        user7.put("id", 7L);
        user7.put("firstName", "Bruno");
        user7.put("lastName", "Alvez");
        user7.put("email", "bruno.alvez@mail.com");
        user7.put("gender", "masculino");

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
                .body("totalItems", equalTo(7))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(0))
                .statusCode(Response.Status.OK.getStatusCode());
    }
    @Order(5)
    @DisplayName("[GET] Get All Users paginated with page size changed to 10 ")
    @Test
    void userPaginatedQueryPageSizeChanged(){
        RestAssured.given()
                .queryParam("size",10)
                .when()
                .get("user/userpaginated")
                .then()
                .body("totalItems", equalTo(7))
                .body("totalPages", equalTo(1))
                .body("currentPage", equalTo(0))
                .statusCode(Response.Status.OK.getStatusCode());
    }
    @Order(6)
    @DisplayName("[GET] Get All Users paginated with actual page changed to second")
    @Test
    void userPaginatedQueryActualPageChanged(){
        RestAssured.given()
                .queryParam("page",1)
                .when()
                .get("user/userpaginated")
                .then()
                .body("totalItems", equalTo(7))
                .body("totalPages", equalTo(2))
                .body("currentPage", equalTo(1))
                .statusCode(Response.Status.OK.getStatusCode());
    }
    @Order(7)
    @DisplayName("[GET] Get All Users paginated with sorted changed by firstName")
    @Test
    void userPaginatedQuerySortChanged(){
     String json =   RestAssured.given()
                .queryParam("sort","firstName")
                .when()
                .get("user/userpaginated")
             .asString();

//        String completeResponse = JsonPath.read(json, "$").toString();
//        System.out.println("-----completeResponse---");
//        System.out.println(completeResponse);
//        String allFirstNames = JsonPath.read(json, "$..firstName").toString();
//        System.out.println("-----allFirstNames---");
//        System.out.println(allFirstNames);
        String firstArrayItem = JsonPath.read(json, "$.users[0].firstName").toString();
//        System.out.println("-----firstArrayItem---");
//        System.out.println(firstArrayItem);
        Assertions.assertTrue(firstArrayItem.contains("Alexandre"));

    }

    @Order(8)
    @DisplayName("[GET] Get All Users paginated with order changed to descending")
    @Test
    void userPaginatedQueryOrderChanged(){
        String json =   RestAssured.given()
                .queryParam("sort","firstName")
                .queryParam("order","Descending")
                .when()
                .get("user/userpaginated")
                .asString();

        String firstArrayItem = JsonPath.read(json, "$.users[0].firstName").toString();
        Assertions.assertTrue(firstArrayItem.contains("karine"));

    }
    @Order(9)
    @DisplayName("[GET] Get All Users paginated and filtered by firstName with letter 'm'")
    @Test
    void userPaginatedQueryFilterFirstNameByPartialStringIgnoreCase(){
        String json =   RestAssured.given()
                .queryParam("firstname","m")
                .when()
                .get("user/userpaginated")
                .asString();

        String ArrayItem = JsonPath.read(json, "$..firstName").toString();

        Assertions.assertTrue(ArrayItem.contains("Myke"));
       Assertions.assertTrue(ArrayItem.contains("Michele"));
    }
    @Order(10)
    @DisplayName("[GET] Get All Users paginated and filtered by lastName with letter 'o'")
    @Test
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
    @Order(11)
    @DisplayName("[GET] Get All Users paginated and filtered by firstName with letter 'm' and lastName 'o'")
    @Test
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

    @Order(12)
    @DisplayName("[PUT] Update User")
    @Test
    void updateUser(){
        JsonObject user = new JsonObject();
        user.put("id", 1L);
        user.put("firstName", "John2");
        user.put("lastName", "Spencer2");
        user.put("email", "jhon2.spencer@mail.com");
        user.put("gender", "masculino2");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user.toString())
                .when()
                .put("user/1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        RestAssured.given()
                .when()
                .get("user/1")
                .then()
                .body("firstName", equalTo("John2"))
                .body("lastName", equalTo("Spencer2"))
                .body("email", equalTo("jhon2.spencer@mail.com"))
                .body("gender", equalTo("masculino2"))
                .statusCode(Response.Status.OK.getStatusCode());
    }
    @Order(13)
    @DisplayName("[DELETE] delete user by id")
   @Test
    void deleteUserById(){
        RestAssured.given()
                .when()
                .delete("user/1")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
        RestAssured.given()
                .when()
                .get("user/1")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

}
