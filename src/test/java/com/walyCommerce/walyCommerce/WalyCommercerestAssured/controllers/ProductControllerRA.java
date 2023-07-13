package com.walyCommerce.walyCommerce.WalyCommercerestAssured.controllers;


import com.walyCommerce.walyCommerce.WalyCommercerestAssured.tests.TokenUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.el.parser.Token;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.print.attribute.standard.JobKOctets;
import java.util.*;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {

    private Long existingProductId, dependencyProductId,nonExistingProductId;
    private String  adminUsername, adminPassword, adminToken, clientUsername, clientPassword, invalidToken, clientToken;
    private Map<String, Object> postProductInstance;

    @BeforeEach
    void setUp() throws Exception{
        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";
        invalidToken = adminToken + "xpto";
        existingProductId = 2L;
        nonExistingProductId = 100L;
        dependencyProductId = 3L;
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        baseURI = "http://localhost:8080";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "meu produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 2000.00);

        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        Map<String, Object> category2 = new HashMap<>();
        category1.put("id", 2);
        category2.put("id", 3);
        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);
    }

    @Test
    void findByIdShouldReturnProductWhenIdExists() {

        given()
                        .get("/products/{id}", existingProductId)
                .then()
                    .statusCode(200)
                    .body("id", is(2))
                    .body("name", equalTo("Smart TV"))
                    .body("price", is(2190.0F))
                    .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("categories.id", hasItems(2,3))
                .body("categories.name", hasItems("Computadores", "Eletrônicos"));
    }

    @Test
    void findAllShouldReturnPageProducts() {

        given()
                .get("/products")
                .then()
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
    }

    @Test
    void findAllSholdReturnPageProductsFilterByName() {

        given()
                .get("/products?name=mac")
                .then()
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"));
    }

    @Test
    void findAllShouldReturnPageProductsWithPriceGreaterThan2000() {
        given()
                .when()
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll { it.price > 2000 }.price", everyItem(greaterThanOrEqualTo(2000.0F)))
                .body("content.findAll {it.price}.name", hasItems("Smart TV", "PC Gamer Weed"));
    }

    @Test
    void insertShouldReturnProductCreatedWhenAdminLoggedAndValidData() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer "+ adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("meu produto"))
                .body("price", is(2000.00F))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2,3));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenInvalidName() {

        postProductInstance.put("name", "e");
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors[0].fieldname", equalTo("name"))
                .body("errors[0].message", equalTo("Nome precisa ter entre 3 e 80 caractere s"));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenInvalidDescription() {

        postProductInstance.put("description", "");
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors[0].fieldname", equalTo("description"))
                .body("errors[0].message", equalTo("Descrição deve ter no minimo 10 caracteres"));

    }
    @Test
    void insertShouldReturnUnprocessableEntityWhenNegativePrice() {

        postProductInstance.put("price", -200.00);
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors[0].fieldname", equalTo("price"))
                .body("errors[0].message", equalTo("O preço deve ser positivo"));

    }
    @Test
    void insertShouldReturnUnprocessableEntityWhenPriceIZero() {

        postProductInstance.put("price", 0.0);
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors[0].fieldname", equalTo("price"))
                .body("errors[0].message", equalTo("O preço deve ser positivo"));

    }
    @Test
    void insertShouldReturnUnprocessableEntityWhenInvalidCategories() {

        postProductInstance.put("categories", null);
        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors[0].fieldname", equalTo("categories"))
                .body("errors[0].message", equalTo("Deve ser adicionado pelo menos uma categoria"));

    }

    @Test
    void insertShouldReturnForbiddenWhenClientLogged() {

        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(403);

    }

    @Test
    void insertShouldReturnUnauthorizedWhenInvalidToken() {

        JSONObject newProduct = new JSONObject(postProductInstance);
        given()
                .header("Content-type", "application-json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(401);

    }

    @Test
    void deleteShouldReturnNotFoundWhenAdminLoggedAndIdDoesNotExisting() {

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/products/{id}", nonExistingProductId)
                .then()
                .statusCode(404);

    }

    @Test
    void deleteShouldReturnBadRequestWhenAdminLoggedAndDependencyId() {

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/products/{id}", dependencyProductId)
                .then()
                .statusCode(400);

    }
    @Test
    void deleteShouldReturnForbiddenWhenClientLogged() {

        given()
                .header("Authorization", "Bearer " + clientToken)
                .when()
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(403);

    }
    @Test
    void deleteShouldReturnUnauthorizedWhenInvalidToken() {

        given()
                .header("Authorization", "Bearer " + invalidToken)
                .when()
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(401);

    }
}
