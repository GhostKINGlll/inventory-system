package com.inventory.dao;

import com.inventory.entity.TieredPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TieredPriceRepository extends JpaRepository<TieredPrice, Integer> {
    List<TieredPrice> findByProductId(Integer productId);
    
    @Query("SELECT t FROM TieredPrice t WHERE t.product.id = :productId AND t.customer.id = :customerId ORDER BY t.minQuantity DESC")
    List<TieredPrice> findSpecificCustomerPrices(@Param("productId") Integer productId, @Param("customerId") Integer customerId);

    @Query("SELECT t FROM TieredPrice t WHERE t.product.id = :productId AND t.customer IS NULL ORDER BY t.minQuantity DESC")
    List<TieredPrice> findDefaultPrices(@Param("productId") Integer productId);
}
