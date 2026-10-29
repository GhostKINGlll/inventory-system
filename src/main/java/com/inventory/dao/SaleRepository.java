package com.inventory.dao;

import com.inventory.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
    List<Sale> findByUserId(Integer userId);
    List<Sale> findByCustomerEntityIdAndUserId(Integer customerEntityId, Integer userId);
    List<Sale> findBySaleDateBetweenAndUserId(LocalDate startDate, LocalDate endDate, Integer userId);
    Optional<Sale> findByIdAndUserId(Integer id, Integer userId);

    @Query(value = "SELECT DATE(s.sale_date) as label, COUNT(*) as saleCount, " +
            "SUM(s.total_amount) as revenue, " +
            "SUM(s.quantity * p.cost_price) as cost, " +
            "(SUM(s.total_amount) - SUM(s.quantity * p.cost_price)) as profit " +
            "FROM sale s JOIN product p ON s.product_id = p.id " +
            "WHERE s.user_id = :userId AND s.sale_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.sale_date) ORDER BY label",
            nativeQuery = true)
    List<Object[]> getDailyProfitStats(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("userId") Integer userId);

    @Query(value = "SELECT YEARWEEK(s.sale_date, 1) as label, COUNT(*) as saleCount, " +
            "SUM(s.total_amount) as revenue, " +
            "SUM(s.quantity * p.cost_price) as cost, " +
            "(SUM(s.total_amount) - SUM(s.quantity * p.cost_price)) as profit " +
            "FROM sale s JOIN product p ON s.product_id = p.id " +
            "WHERE s.user_id = :userId AND s.sale_date BETWEEN :startDate AND :endDate " +
            "GROUP BY YEARWEEK(s.sale_date, 1) ORDER BY label",
            nativeQuery = true)
    List<Object[]> getWeeklyProfitStats(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("userId") Integer userId);

    @Query(value = "SELECT DATE_FORMAT(s.sale_date, '%Y-%m') as label, COUNT(*) as saleCount, " +
            "SUM(s.total_amount) as revenue, " +
            "SUM(s.quantity * p.cost_price) as cost, " +
            "(SUM(s.total_amount) - SUM(s.quantity * p.cost_price)) as profit " +
            "FROM sale s JOIN product p ON s.product_id = p.id " +
            "WHERE s.user_id = :userId AND s.sale_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE_FORMAT(s.sale_date, '%Y-%m') ORDER BY label",
            nativeQuery = true)
    List<Object[]> getMonthlyProfitStats(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         @Param("userId") Integer userId);
}