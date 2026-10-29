package com.inventory.dao;

import com.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByUserId(Integer userId);
    List<Product> findByNameAndUserId(String name, Integer userId);
    List<Product> findByNameContainingAndUserId(String name, Integer userId);
    Optional<Product> findByIdAndUserId(Integer id, Integer userId);

    @Query("SELECT p FROM Product p WHERE p.userId = :userId AND (p.name LIKE %:keyword% OR p.specification LIKE %:keyword%)")
    List<Product> search(String keyword, Integer userId);

    @Query(value = "SELECT p.* FROM product p WHERE p.user_id = :userId AND (SELECT COALESCE(SUM(s.stock_quantity), 0) FROM product_sku s WHERE s.product_id = p.id) <= p.min_stock", nativeQuery = true)
    List<Product> findLowStockProducts(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(MIN(p.id), 0) FROM Product p WHERE p.userId = :userId")
    Integer findMinIdByUserId(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Product p WHERE p.userId = :userId")
    Integer findMaxIdByUserId(@Param("userId") Integer userId);
}