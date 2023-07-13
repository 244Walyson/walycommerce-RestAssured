package com.walyCommerce.walyCommerce.WalyCommercerestAssured.controllers;

import com.walyCommerce.walyCommerce.WalyCommercerestAssured.tests.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword, adminToken, clientToken, invalidToken;
    private Long existingOrderId, nonExistingOrderId;

    @BeforeEach
    void setUp() {
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";
        clientUsername = "maria@gmail.com";
        clientPassword  = "123456";
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        invalidToken = adminToken + "xpto";
        existingOrderId = 1L;
        nonExistingOrderId = 100L;

    }

    @Test
    void findByIdShouldReturnOrderWhenExistsOrderAndAdminLogged() {

        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.id", is(1))
                .body("client.name", equalTo("maria@gmail.com"))
                .body("items.name", hasItems("The Lord of the Rings"));
    }

    @Test
    void findByIdShouldReturnOrderWhenExistsOrderAndClientLoggedAndOrderBelonging() {

        given()
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.id", is(1))
                .body("client.name", equalTo("maria@gmail.com"));
    }

    @Test
    void findByIdShouldReturnForbiddenWhenExistsOrderAndClientLoggedAndOrderNotBelonging() {
        existingOrderId = 2L;
        given()
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(403);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenDoesNotExistsOrderAndAdminLogged() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenDoesNotExistsOrderAndClientLogged() {
        given()
                .header("Authorization", "Bearer " + clientToken)
                .get("/orders/{id}", nonExistingOrderId)
                .then()
                .statusCode(404);
    }
    @Test
    void findByIdShouldReturnUnauthorizedWhenInvalidToke(){
        given()
                .header("Authorization", "Bearer " + invalidToken)
                .get("/orders/{id}", existingOrderId)
                .then()
                .statusCode(401);
    }
}
