package com.example.bankcards.dto;

import com.example.bankcards.util.enums.CardStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDto {
    private Long id;

    @NotBlank
    private String maskedCardNumber;

    @NotBlank
    private String cardHolder;

    @Future
    private LocalDate expiryDate;
    private CardStatus status;

    @PositiveOrZero
    private BigDecimal balance;

}