package com.inventory.service;

import com.inventory.dao.SampleOrderRepository;
import com.inventory.entity.SampleOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SampleOrderService {
    private final SampleOrderRepository sampleOrderRepository;

    public SampleOrderService(SampleOrderRepository sampleOrderRepository) {
        this.sampleOrderRepository = sampleOrderRepository;
    }

    public List<SampleOrder> findAll(Integer userId) {
        return sampleOrderRepository.findByUserId(userId);
    }

    public Optional<SampleOrder> findById(Integer id, Integer userId) {
        return sampleOrderRepository.findByIdAndUserId(id, userId);
    }

    @Transactional
    public SampleOrder save(SampleOrder sampleOrder) {
        return sampleOrderRepository.save(sampleOrder);
    }

    @Transactional
    public void updateStatus(Integer id, String status, Integer userId) {
        sampleOrderRepository.findByIdAndUserId(id, userId).ifPresent(order -> {
            order.setStatus(status);
            sampleOrderRepository.save(order);
        });
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        sampleOrderRepository.findByIdAndUserId(id, userId).ifPresentOrElse(
            order -> sampleOrderRepository.deleteById(id),
            () -> { throw new RuntimeException("无权操作"); }
        );
    }
}
