package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserDetailsImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {
    private final CardService cardService;
    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto,
                                              Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        Card card = new Card();
        card.setCardNumber(cardDto.getMaskedCardNumber());
        card.setCardHolder(cardDto.getCardHolder());
        card.setExpiryDate(cardDto.getExpiryDate());
        card.setBalance(cardDto.getBalance());
        CardDto createdCard = cardService.createCard(card, user);

        return ResponseEntity.ok(createdCard);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        Page<CardDto> cards = cardService.getUserCardsWithFilter(user, search, status, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id,
                                               Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        CardDto card = cardService.getCardById(id, user);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getAllUserCards(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        List<CardDto> cards = cardService.getAllUserCards(user);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id,
                                              @Valid @RequestBody CardDto cardDto,
                                              Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        Card card = new Card();
        card.setCardHolder(cardDto.getCardHolder());
        card.setExpiryDate(cardDto.getExpiryDate());

        CardDto updatedCard = cardService.updateCard(id, card, user);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id,
                                           Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        cardService.deleteCard(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id,
                                             Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        CardDto blockedCard = cardService.blockCard(id, user);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id,
                                                Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        CardDto activatedCard = cardService.activateCard(id, user);
        return ResponseEntity.ok(activatedCard);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> transferBetweenCards(
            @Valid @RequestBody TransferRequest transferRequest,
            Authentication authentication)
            throws CardNotFoundException, UnauthorizedCardAccessException,
            InsufficientFundsException, CardBlockedException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());

        cardService.transferBetweenCards(transferRequest, user);
        return ResponseEntity.ok().build();
    }
}