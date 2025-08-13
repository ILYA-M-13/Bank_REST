package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.enums.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findByIdAndUser(Long id, User user);

    @Query("SELECT c FROM Card c WHERE c.user = :user AND " +
            "(LOWER(c.cardHolder) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.status = :status)")
    Page<Card> findByUserAndSearch(@Param("user") User user,
                                   @Param("search") String search,
                                   @Param("status") CardStatus status,
                                   Pageable pageable);
}