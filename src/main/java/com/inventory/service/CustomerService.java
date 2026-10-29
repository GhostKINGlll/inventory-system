package com.inventory.service;

import com.inventory.dao.CustomerRepository;
import com.inventory.entity.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll(Integer userId) {
        return customerRepository.findByUserId(userId);
    }

    public Optional<Customer> findById(Integer id, Integer userId) {
        return customerRepository.findById(id);
    }

    @Transactional
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        customerRepository.findById(id).ifPresentOrElse(
            customer -> customerRepository.deleteById(id),
            () -> { throw new RuntimeException("无权操作该客户"); }
        );
    }
}
