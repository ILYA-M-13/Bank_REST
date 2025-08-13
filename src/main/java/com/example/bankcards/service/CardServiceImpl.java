package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMasker;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.enums.CardStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService{
    private final CardRepository cardRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardMasker cardMasker;

    @Override
    @Transactional
    public CardDto createCard(Card card, User user) {
        card.setCardNumber(encryptionUtil.encrypt(card.getCardNumber()));
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }

    @Override
    @Transactional
    public CardDto updateCard(Long id, Card card, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        Card existingCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!existingCard.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCardAccessException("You are not authorized to update this card");
        }

        existingCard.setCardHolder(card.getCardHolder());
        existingCard.setExpiryDate(card.getExpiryDate());

        Card updatedCard = cardRepository.save(existingCard);
        return convertToDto(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long id, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCardAccessException("You are not authorized to delete this card");
        }

        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public CardDto blockCard(Long id, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCardAccessException("You are not authorized to block this card");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return convertToDto(updatedCard);
    }

    @Override
    @Transactional
    public CardDto activateCard(Long id, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCardAccessException("You are not authorized to activate this card");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
        } else {
            card.setStatus(CardStatus.ACTIVE);
        }

        Card updatedCard = cardRepository.save(card);
        return convertToDto(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardById(Long id, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedCardAccessException("You are not authorized to view this card");
        }

        return convertToDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getAllUserCards(User user) {
        return cardRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCardsWithFilter(User user, String search, String status, Pageable pageable) {
        CardStatus cardStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                cardStatus = CardStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }

        Page<Card> cardsPage = cardRepository.findByUserAndSearch(
                user,
                search != null ? search : "",
                cardStatus,
                pageable);

        return cardsPage.map(this::convertToDto);
    }

    @Override
    @Transactional
    public void transferBetweenCards(TransferRequest transferRequest, User user)
            throws CardNotFoundException, UnauthorizedCardAccessException,
            InsufficientFundsException, CardBlockedException {
        Card fromCard = cardRepository.findByIdAndUser(transferRequest.getFromCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("Source card not found or not owned by user"));

        Card toCard = cardRepository.findByIdAndUser(transferRequest.getToCardId(), user)
                .orElseThrow(() -> new CardNotFoundException("Destination card not found or not owned by user"));

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException("One of the cards is blocked or expired");
        }

        if (fromCard.getBalance().compareTo( transferRequest.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferRequest.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferRequest.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    private CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(cardMasker.maskCardNumber(encryptionUtil.decrypt(card.getCardNumber())));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }
}