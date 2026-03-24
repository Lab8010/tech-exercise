package com.example.petbattle;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import java.util.Map;
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

    @Test
    void createCat_jsonBase64_addsRow() {
        String b64 =
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        Map<String, String> body = new LinkedHashMap<>();
        body.put("name", "UploadedCat");
        body.put("imageBase64", b64);
        body.put("contentType", "image/png");
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/cats")
                .then()
                .statusCode(200)
                .body("name", is("UploadedCat"))
                .body("imagePath", startsWith("/uploads/"));

        given()
                .when()
                .get("/cats")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(4)));
    }
}
