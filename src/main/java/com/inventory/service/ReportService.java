package com.inventory.service;

import com.inventory.dao.SaleRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReportService {
    private final SaleRepository saleRepository;

    public ReportService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Map<String, Object>> getProfitStats(LocalDate startDate, LocalDate endDate, Integer userId, String groupBy) {
        List<Object[]> rawResults;
        if ("week".equals(groupBy)) {
            rawResults = saleRepository.getWeeklyProfitStats(startDate, endDate, userId);
        } else if ("month".equals(groupBy)) {
            rawResults = saleRepository.getMonthlyProfitStats(startDate, endDate, userId);
        } else {
            rawResults = saleRepository.getDailyProfitStats(startDate, endDate, userId);
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Object[] row : rawResults) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", row[0].toString());
            map.put("saleCount", ((Number) row[1]).intValue());
            map.put("revenue", new BigDecimal(row[2].toString()));
            map.put("cost", new BigDecimal(row[3].toString()));
            BigDecimal profit = new BigDecimal(row[4].toString());
            map.put("profit", profit);
            BigDecimal revenue = new BigDecimal(row[2].toString());
            if (revenue.compareTo(BigDecimal.ZERO) > 0) {
                map.put("profitMargin", profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            } else {
                map.put("profitMargin", BigDecimal.ZERO);
            }
            results.add(map);
        }
        return results;
    }

    public Map<String, Object> getSummary(LocalDate startDate, LocalDate endDate, Integer userId) {
        List<Map<String, Object>> stats = getProfitStats(startDate, endDate, userId, "day");
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalSales = 0;

        for (Map<String, Object> row : stats) {
            totalRevenue = totalRevenue.add((BigDecimal) row.get("revenue"));
            totalCost = totalCost.add((BigDecimal) row.get("cost"));
            totalProfit = totalProfit.add((BigDecimal) row.get("profit"));
            totalSales += (Integer) row.get("saleCount");
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalSales", totalSales);
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalCost", totalCost);
        summary.put("totalProfit", totalProfit);
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            summary.put("profitMargin", totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        } else {
            summary.put("profitMargin", BigDecimal.ZERO);
        }
        return summary;
    }
}
