package ru.netology.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.netology.data.Card;
import ru.netology.data.DataHelper;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class ApiInteraction {
    private final String baseUri = "http://localhost:7777/api/";
    private final String type = "application/json";

    public String login(String login, String password) {
        var authInfo = new DataHelper.AuthInfo(login, password);

        String statusCode = // @formatter:off
        given()
            .baseUri(baseUri)
            .contentType(type)
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
            .baseUri(baseUri)
            .contentType(type)
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
            .baseUri(baseUri)
            .contentType(type)
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

        var mapper = new ObjectMapper();
        return List.of(mapper.readValue(response, Card[].class));
    }

    public void transfer(String token, String fromCard, String toCard, int amount) {
        var transferInfo = new DataHelper.TransferInfo(fromCard, toCard, amount);

        // @formatter:off
        given()
            .baseUri(baseUri)
            .contentType(type)
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