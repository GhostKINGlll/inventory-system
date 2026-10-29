package com.inventory.dao;

import com.inventory.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Integer> {
    List<ProductSku> findByProductId(Integer productId);
    Optional<ProductSku> findByBarcodeAndProductUserId(String barcode, Integer userId);
    
    @Query("SELECT s FROM ProductSku s WHERE s.product.id = :productId AND s.color = :color AND s.size = :size")
    Optional<ProductSku> findByProductAndSpecs(@Param("productId") Integer productId, @Param("color") String color, @Param("size") String size);
}
