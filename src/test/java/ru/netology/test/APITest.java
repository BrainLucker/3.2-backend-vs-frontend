package ru.netology.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.api.ApiInteraction;
import ru.netology.data.Card;
import ru.netology.data.DataGenerator;
import ru.netology.db.DbInteraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/* Запуск SUT:
java -jar .\artifacts\app-deadline.jar
-P:jdbc.url=jdbc:mysql://localhost:3306/app -P:jdbc.user=admin -P:jdbc.password=pass -port=7777
 */

public class APITest {
    private static final DbInteraction db = new DbInteraction();
    private static final ApiInteraction api = new ApiInteraction();
    private String id;
    private String login;
    private String password;

    private String loginAndVerify() {
        api.login(login, password);
        var code = db.getVerificationCode(login);
        return api.verify(login, code);
    }

    @BeforeEach
    private void generateNewUser() { // генерируем и добавляем нового пользователя в БД
        var userInfo = DataGenerator.Registration.generateActiveUser();
        db.addUser(userInfo);
        id = userInfo.getId();
        login = userInfo.getLogin();
        password = userInfo.getPassword();
    }

    @Test
    public void shouldLogin() {
        var status = api.login(login, password);
        assertThat(status, containsString("200 OK"));
    }

    @Test
    public void shouldVerify() {
        var token = loginAndVerify();
        assertThat(token, matchesRegex("^\\w+\\.\\w+\\..*$"));
    }

    @Test
    public void shouldGetCards() {
        var firstCard = DataGenerator.Registration.generateCard(1000_00); // генерируем карты
        var secondCard = DataGenerator.Registration.generateCard(2000_00);
        db.addCard(id, firstCard); // добавляем карты в БД для текущего пользователя
        db.addCard(id, secondCard);
        var cardsExpected = new ArrayList<>(Arrays.asList(secondCard.encryptData(), firstCard.encryptData()));

        var cardsFromDB = db.getUsersCards(id); // получаем список карт из БД для текущего пользователя
        var token = loginAndVerify();
        var cardsFromAPI = api.getCards(token); // получаем список карт из тела ответа для текущего пользователя

        assertTrue(cardsFromDB.containsAll(cardsExpected));
        assertTrue(cardsFromAPI.containsAll(cardsExpected));
    }

    @Test
    public void shouldTransferBetweenOwnCards() {
        var firstCardBalance = 1000_00;
        var secondCardBalance = 1000_00;
        var amount = 500_00;

        var firstCard = DataGenerator.Registration.generateCard(firstCardBalance); // генерируем карты
        var secondCard = DataGenerator.Registration.generateCard(secondCardBalance);
        db.addCard(id, firstCard); // добавляем карты в БД
        db.addCard(id, secondCard);
        var token = loginAndVerify();
        api.transfer(token, firstCard.getNumber(), secondCard.getNumber(), amount / 100); // совершаем перевод через API
        var cardsActual = db.getUsersCards(id); // получаем список карт из БД

        firstCard.setBalance(firstCardBalance - amount); // меняем ожидаемые баланс карт на величину перевода
        secondCard.setBalance(secondCardBalance + amount);
        var cardsExpected = new ArrayList<>(Arrays.asList(secondCard.encryptData(), firstCard.encryptData()));

        assertTrue(cardsActual.containsAll(cardsExpected));
    }

    @Test
    public void shouldTransferToAnyCard() {
        var userCardBalance = 1000_00;
        var recipientCardBalance = 1000_00;
        var amount = 500_00;

        var userCard = DataGenerator.Registration.generateCard(userCardBalance); // генерируем карты
        var recipientCard = DataGenerator.Registration.generateCard(recipientCardBalance);
        db.addCard(id, userCard); // добавляем карту пользователя в БД
        var token = loginAndVerify();
        api.transfer(token, userCard.getNumber(), recipientCard.getNumber(), amount / 100); // совершаем перевод через API
        var cardsActual = db.getUsersCards(id).get(0); // получаем карту из БД

        userCard.setBalance(userCardBalance - amount); // меняем ожидаемый баланс карты на величину перевода
        var cardExpected = userCard.encryptData();

        assertEquals(cardExpected, cardsActual);
    }

    @Test
    public void shouldNotTransferFromAnyCard() {
        var userCardBalance = 1000_00;
        var senderCardBalance = 1000_00;
        var amount = 500_00;

        var userCard = DataGenerator.Registration.generateCard(userCardBalance); // генерируем карты
        var senderCard = DataGenerator.Registration.generateCard(senderCardBalance);
        db.addCard(id, userCard); // добавляем карту пользователя в БД
        var token = loginAndVerify();
        api.transfer(token, senderCard.getNumber(), userCard.getNumber(),amount / 100); // совершаем перевод через API
        var cardsActual = db.getUsersCards(id).get(0); // получаем карту из БД

        var cardExpected = userCard.encryptData();

        assertEquals(cardExpected, cardsActual);
    }
}