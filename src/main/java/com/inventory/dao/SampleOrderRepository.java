package com.inventory.dao;

import com.inventory.entity.SampleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SampleOrderRepository extends JpaRepository<SampleOrder, Integer> {
    List<SampleOrder> findByUserId(Integer userId);
    Optional<SampleOrder> findByIdAndUserId(Integer id, Integer userId);
}
