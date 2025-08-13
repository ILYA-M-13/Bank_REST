package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMasker {
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }

        String digitsOnly = cardNumber.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 4) {
            return cardNumber;
        }

        String masked = digitsOnly.replaceAll(".(?=.{4})", "*");

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < masked.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(masked.charAt(i));
        }

        return formatted.toString();
    }
}