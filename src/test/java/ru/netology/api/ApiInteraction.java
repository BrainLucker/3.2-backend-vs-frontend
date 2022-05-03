package ru.netology.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import ru.netology.data.Card;
import ru.netology.data.DataHelper;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ApiInteraction {
    private static final RequestSpecification requestSpec = new RequestSpecBuilder() // @formatter:off
        .setBaseUri("http://localhost/api/")
        .setPort(7777)
        .setAccept(ContentType.JSON)
        .setContentType("application/json")
        .log(LogDetail.ALL)
        .build()
        ; // @formatter:on

    public String login(String login, String password) {
        var authInfo = new DataHelper.AuthInfo(login, password);

        String statusCode = // @formatter:off
        given()
            .spec(requestSpec)
            .body(authInfo)
        .when()
            .post("/auth")
        .then()
            .statusCode(200)
        .extract()
            .statusLine()
        ; // @formatter:on

        return statusCode;
    }

    public String verify(String login, String code) {
        var verifyInfo = new DataHelper.VerifyInfo(login, code);

        String token = // @formatter:off
        given()
            .spec(requestSpec)
            .body(verifyInfo)
        .when()
            .post("/auth/verification")
        .then()
            .statusCode(200)
        .extract()
            .path("token")
        ; // @formatter:on

        return token;
    }

    @SneakyThrows
    public List<Card> getCards(String token) {

        String response = // @formatter:off
        given()
            .spec(requestSpec)
        .auth()
            .oauth2(token)
        .when()
            .get("/cards")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("cards.schema.json"))
        .extract()
            .response().getBody().asString()
        ; // @formatter:on

        return List.of(new ObjectMapper().readValue(response, Card[].class));
    }

    public void transfer(String token, String fromCard, String toCard, int amount) {
        var transferInfo = new DataHelper.TransferInfo(fromCard, toCard, amount);
        // @formatter:off
        given()
            .spec(requestSpec)
            .body(transferInfo)
        .auth()
            .oauth2(token)
        .when()
            .post("/transfer")
        .then()
            .statusCode(200)
        .extract()
            .statusLine()
        ; // @formatter:on
    }
}