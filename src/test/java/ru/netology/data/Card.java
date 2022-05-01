package ru.netology.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Card {
    private String id;
    private String number;
    private int balance;

    public Card encryptData() {
        int length = this.number.length();
        this.number = "**** **** **** " + this.number.substring(length - 4, length);
        this.balance /= 100;
        return this;
    }
}