package com.inventory.service;

import com.inventory.dao.TieredPriceRepository;
import com.inventory.entity.TieredPrice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TieredPriceService {
    private final TieredPriceRepository tieredPriceRepository;

    public TieredPriceService(TieredPriceRepository tieredPriceRepository) {
        this.tieredPriceRepository = tieredPriceRepository;
    }

    public List<TieredPrice> findPricesByProduct(Integer productId) {
        return tieredPriceRepository.findByProductId(productId);
    }

    public BigDecimal getPrice(Integer productId, Integer customerId, Integer quantity) {
        List<TieredPrice> prices;
        if (customerId != null) {
            prices = tieredPriceRepository.findSpecificCustomerPrices(productId, customerId);
            for (TieredPrice p : prices) {
                if (quantity >= p.getMinQuantity()) return p.getPrice();
            }
        }
        prices = tieredPriceRepository.findDefaultPrices(productId);
        for (TieredPrice p : prices) {
            if (quantity >= p.getMinQuantity()) return p.getPrice();
        }
        return null;
    }

    @Transactional
    public void saveAll(Integer productId, List<TieredPrice> prices) {
        tieredPriceRepository.findByProductId(productId).forEach(p -> tieredPriceRepository.delete(p));
        for (TieredPrice p : prices) {
            p.getProduct().setId(productId);
            tieredPriceRepository.save(p);
        }
    }
}
