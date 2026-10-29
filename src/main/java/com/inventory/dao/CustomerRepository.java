package com.inventory.dao;

import com.inventory.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByUserId(Integer userId);
    Optional<Customer> findByNameAndUserId(String name, Integer userId);
}
