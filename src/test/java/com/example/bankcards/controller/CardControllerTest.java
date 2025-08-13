package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserDetailsImpl;
import com.example.bankcards.util.enums.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private Authentication authentication;
    private UserDetailsImpl userDetails;
    private CardDto testCardDto;

    @BeforeEach
    void setUp() {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER"));

        userDetails = new UserDetailsImpl(
                1L,
                "user@example.com",
                "John Doe",
                "password",
                authorities);

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        testCardDto = new CardDto();
        testCardDto.setId(1L);
        testCardDto.setMaskedCardNumber("123456******7890");
        testCardDto.setCardHolder("John Doe");
        testCardDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        testCardDto.setStatus(CardStatus.ACTIVE);
        testCardDto.setBalance(BigDecimal.valueOf(1000.0));
    }

    @Test
    void createCard_AdminRole_Success() {
        when(cardService.createCard(any(), any())).thenReturn(testCardDto);

        ResponseEntity<CardDto> response = cardController.createCard(testCardDto, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("123456******7890", response.getBody().getMaskedCardNumber());
        verify(cardService).createCard(any(), any());
    }

    @Test
    void getUserCards_WithFilter_Success() {
        Page<CardDto> page = new PageImpl<>(List.of(testCardDto));
        when(cardService.getUserCardsWithFilter(any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<CardDto>> response = cardController.getUserCards(
                "search", "ACTIVE", Pageable.unpaged(), authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("John Doe", response.getBody().getContent().get(0).getCardHolder());
        verify(cardService).getUserCardsWithFilter(any(), any(), any(), any());
    }

    @Test
    void getCardById_Success() throws CardNotFoundException, UnauthorizedCardAccessException {
        when(cardService.getCardById(eq(1L), any(User.class))).thenReturn(testCardDto);

        ResponseEntity<CardDto> response = cardController.getCardById(1L, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(LocalDate.of(2025, 12, 31), response.getBody().getExpiryDate());
        verify(cardService).getCardById(eq(1L), any(User.class));
    }

    @Test
    void getAllUserCards_Success() {
        List<CardDto> cards = List.of(testCardDto);
        when(cardService.getAllUserCards(any())).thenReturn(cards);

        ResponseEntity<List<CardDto>> response = cardController.getAllUserCards(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(BigDecimal.valueOf(1000.0), response.getBody().get(0).getBalance());
        verify(cardService).getAllUserCards(any());
    }

    @Test
    void updateCard_Success() throws CardNotFoundException, UnauthorizedCardAccessException {
        CardDto updatedCardDto = new CardDto();
        updatedCardDto.setCardHolder("John Doe Updated");
        updatedCardDto.setExpiryDate(LocalDate.of(2026, 12, 31));

        testCardDto.setCardHolder("John Doe Updated");
        testCardDto.setExpiryDate(LocalDate.of(2026, 12, 31));

        when(cardService.updateCard(eq(1L), any(), any())).thenReturn(testCardDto);

        ResponseEntity<CardDto> response = cardController.updateCard(1L, updatedCardDto, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe Updated", response.getBody().getCardHolder());
        assertEquals(LocalDate.of(2026, 12, 31), response.getBody().getExpiryDate());
        verify(cardService).updateCard(eq(1L), any(), any());
    }

    @Test
    void deleteCard_AdminRole_Success() throws CardNotFoundException, UnauthorizedCardAccessException {

        doNothing().when(cardService).deleteCard(eq(1L), any(User.class));

        ResponseEntity<Void> response = cardController.deleteCard(1L, authentication);

        assertEquals(204, response.getStatusCodeValue());
        verify(cardService).deleteCard(eq(1L), any(User.class));
    }

    @Test
    void blockCard_Success() throws CardNotFoundException, UnauthorizedCardAccessException {
        testCardDto.setStatus(CardStatus.BLOCKED);
        when(cardService.blockCard(eq(1L), any(User.class))).thenReturn(testCardDto);

        ResponseEntity<CardDto> response = cardController.blockCard(1L, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(CardStatus.BLOCKED, response.getBody().getStatus());
        verify(cardService).blockCard(eq(1L), any(User.class));
    }

    @Test
    void activateCard_Success() throws CardNotFoundException, UnauthorizedCardAccessException {
        testCardDto.setStatus(CardStatus.ACTIVE);
        when(cardService.activateCard(eq(1L), any(User.class))).thenReturn(testCardDto);

        ResponseEntity<CardDto> response = cardController.activateCard(1L, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(CardStatus.ACTIVE, response.getBody().getStatus());
        verify(cardService).activateCard(eq(1L), any(User.class));
    }

    @Test
    void transferBetweenCards_Success() throws CardNotFoundException, UnauthorizedCardAccessException,
            InsufficientFundsException, CardBlockedException {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));
        doNothing().when(cardService).transferBetweenCards(eq(request), any(User.class));

        ResponseEntity<Void> response = cardController.transferBetweenCards(request, authentication);

        assertEquals(200, response.getStatusCodeValue());
        verify(cardService).transferBetweenCards(eq(request), any(User.class));
    }

    @Test
    void getCardById_CardNotFoundException() throws CardNotFoundException, UnauthorizedCardAccessException {
        when(cardService.getCardById(eq(1L), any(User.class))).thenThrow(new CardNotFoundException("Card not found"));

        assertThrows(CardNotFoundException.class, () -> {
            cardController.getCardById(1L, authentication);
        });
    }

    @Test
    void getCardById_UnauthorizedCardAccessException() throws CardNotFoundException, UnauthorizedCardAccessException {
        when(cardService.getCardById(eq(1L), any(User.class))).thenThrow(new UnauthorizedCardAccessException("Unauthorized"));

        assertThrows(UnauthorizedCardAccessException.class, () -> {
            cardController.getCardById(1L, authentication);
        });
    }
}