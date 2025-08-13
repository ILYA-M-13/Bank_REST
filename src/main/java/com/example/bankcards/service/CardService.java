package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {
    CardDto createCard(Card card, User user);
    CardDto updateCard(Long id, Card card, User user) throws CardNotFoundException, UnauthorizedCardAccessException;
    void deleteCard(Long id, User user) throws CardNotFoundException, UnauthorizedCardAccessException;
    CardDto blockCard(Long id, User user) throws CardNotFoundException, UnauthorizedCardAccessException;
    CardDto activateCard(Long id, User user) throws CardNotFoundException, UnauthorizedCardAccessException;
    CardDto getCardById(Long id, User user) throws CardNotFoundException, UnauthorizedCardAccessException;
    List<CardDto> getAllUserCards(User user);
    Page<CardDto> getUserCardsWithFilter(User user, String search, String status, Pageable pageable);
    void transferBetweenCards(TransferRequest transferRequest, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException, InsufficientFundsException, CardBlockedException;
}