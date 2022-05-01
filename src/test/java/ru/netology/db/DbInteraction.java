package ru.netology.db;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import ru.netology.data.Card;
import ru.netology.data.DataGenerator;
import ru.netology.mode.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class DbInteraction {

    @SneakyThrows
    private Connection getConnection() {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/app", "admin", "pass");
    }

    private String passwordEncryption(String password) {
        // TODO Добавить алгоритм генерации пароля
        return "$2a$10$TnNjXMBKBO.I3l1rVG70sOJREv/AgKqDpkr5XuiUvY80tlFmhFzF2";
    }

    @SneakyThrows
    public void clearData() {
        var runner = new QueryRunner();

        try (var conn = getConnection()) {
            runner.update(conn, "DELETE from card_transactions;");
            runner.update(conn, "DELETE from cards;");
            runner.update(conn, "DELETE from auth_codes;");
            runner.update(conn, "DELETE from users;");
        }
    }

    @SneakyThrows
    public void addUser(DataGenerator.UserInfo user) {
        var runner = new QueryRunner();
        var dataSQL = "INSERT INTO users(id, login, password, status) VALUES (?, ?, ?, ?);";
        var password = passwordEncryption(user.getPassword()); // шифруем пароль для записи в БД

        try (var conn = getConnection()) {
            runner.update(conn, dataSQL, user.getId(), user.getLogin(), password, user.getStatus());
        }
    }

    @SneakyThrows
    public String getVerificationCode(String login) {
        var runner = new QueryRunner();
        var codeSQL = "SELECT code FROM auth_codes ac " +
                "LEFT JOIN users u ON ac.user_id = u.id " +
                "WHERE u.login = ? ORDER BY ac.created DESC;";

        try (var conn = getConnection()) {
            return runner.query(conn, codeSQL, new ScalarHandler<>(), login);
        }
    }

    @SneakyThrows
    public void addCard(String userId, Card card) {
        var runner = new QueryRunner();
        var cardSQL = "INSERT INTO cards(id, user_id, number, balance_in_kopecks) VALUES (?, ?, ?, ?);";

        try (var conn = getConnection()) {
            runner.update(conn, cardSQL, card.getId(), userId, card.getNumber(), card.getBalance());
        }
    }

    @SneakyThrows
    public List<Card> getUsersCards(String id) {
        var cardsSQL = "SELECT id, number, balance_in_kopecks FROM cards WHERE user_id = ?;";
        var cards = new ArrayList<Card>();

        try (
                var conn = getConnection();
                var codeStmt = conn.prepareStatement(cardsSQL)
        ) {
            codeStmt.setString(1, id);
            try (var rs = codeStmt.executeQuery()) {
                for (int i = 0; rs.next(); i++) { // записываем данные карт пользователя в список карт
                    var card = new Card(
                            rs.getString("id"),
                            rs.getString("number"),
                            rs.getInt("balance_in_kopecks")
                    );
                    cards.add(i, card.encryptData());
                }
            }
        }
        return cards;
    }



}