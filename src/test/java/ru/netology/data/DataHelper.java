package ru.netology.data;

import lombok.Value;

public class DataHelper {

    @Value
    public static class AuthInfo {
        String login;
        String password;
    }

    @Value
    public static class VerifyInfo {
        String login;
        String code;
    }

    @Value
    public static class TransferInfo {
        String from;
        String to;
        int amount;
    }
}
