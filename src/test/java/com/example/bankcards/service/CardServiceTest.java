package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMasker;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.enums.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMasker cardMasker;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setFullName("Test User");

        card = new Card();
        card.setId(1L);
        card.setCardNumber("encrypted-card-number");
        card.setCardHolder("Test User");
        card.setExpiryDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));
        card.setUser(user);
    }

    @Test
    void createCard_Success() {
        Card card = new Card();
        card.setCardNumber("1234567812345678");
        card.setCardHolder("Test User");
        card.setExpiryDate(LocalDate.now().plusYears(2));
        card.setBalance(new BigDecimal("1000.00"));
        card.setStatus(CardStatus.ACTIVE);

        User user = new User();
        user.setId(1L);

        when(encryptionUtil.encrypt(anyString()))
                .thenReturn("encrypted-card-number");

        when(cardMasker.maskCardNumber(isNull()))
                .thenReturn("**** **** **** 1234");

        when(cardRepository.save(any(Card.class)))
                .thenReturn(card);

        CardDto result = cardService.createCard(card, user);

        assertNotNull(result);
        assertEquals("**** **** **** 1234", result.getMaskedCardNumber());
        assertEquals("Test User", result.getCardHolder());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(1000.0, result.getBalance().doubleValue());

        verify(encryptionUtil).encrypt("1234567812345678");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void transferBetweenCards_Success() throws Exception {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        when(cardRepository.findByIdAndUser(eq(1L), eq(user))).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(eq(2L), eq(user))).thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("200.00"));

        cardService.transferBetweenCards(request, user);

        assertEquals(0, new BigDecimal("800.00").compareTo(fromCard.getBalance()));
        assertEquals(0, new BigDecimal("700.0").compareTo( toCard.getBalance()));
    }

    @Test
    void transferBetweenCards_InsufficientFunds() {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(new BigDecimal("100.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setUser(user);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(new BigDecimal("100.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setUser(user);

        when(cardRepository.findByIdAndUser(eq(1L), eq(user))).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdAndUser(eq(2L), eq(user))).thenReturn(Optional.of(toCard));

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("200.00"));

        assertThrows(InsufficientFundsException.class, () -> {
            cardService.transferBetweenCards(request, user);
        });
    }
}