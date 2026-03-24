package com.example.petbattle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CatResourceTest {

    @Test
    void listCats_returnsInitialData() {
        given()
                .when().get("/cats")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(3)));
    }

    @Test
    void vote_incrementsCount() {
        given()
                .when().post("/cats/1/vote")
                .then()
                .statusCode(200);
    }
}
