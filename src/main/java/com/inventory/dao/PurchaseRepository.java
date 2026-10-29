package com.inventory.dao;

import com.inventory.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByUserId(Integer userId);
    List<Purchase> findByPurchaseDateBetweenAndUserId(LocalDate startDate, LocalDate endDate, Integer userId);
    Optional<Purchase> findByIdAndUserId(Integer id, Integer userId);
}