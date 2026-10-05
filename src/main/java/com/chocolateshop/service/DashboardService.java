package com.chocolateshop.service;

import com.chocolateshop.entity.Sale;
import com.chocolateshop.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ExpenseRepository expenseRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleService saleService;

    @Data
    @Builder
    public static class DashboardData {
        private BigDecimal todaySales;
        private long todayOrders;
        private BigDecimal todayProfit;
        private long totalProducts;
        private long lowStockProducts;
        private long totalCustomers;
        private BigDecimal todayExpenses;
        private BigDecimal todayCashBalance;
        private BigDecimal totalReceivableDue;
        private BigDecimal totalPayableDue;
        private List<Sale> recentOrders;
        private List<Object[]> bestSellers;
        private List<String> chartLabels;
        private List<BigDecimal> chartSales;
        private List<BigDecimal> chartProfits;
    }

    @Transactional(readOnly = true)
    public DashboardData getDashboardData() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        BigDecimal todaySales = saleRepository.sumTotalSalesBetween(startOfDay, endOfDay);
        long todayOrders = saleRepository.countOrdersBetween(startOfDay, endOfDay);
        BigDecimal todayGrossProfit = saleRepository.sumProfitBetween(startOfDay, endOfDay);
        BigDecimal todayExpenses = expenseRepository.sumExpensesBetween(today, today);
        BigDecimal todayNetProfit = todayGrossProfit.subtract(todayExpenses);

        BigDecimal todayCashSales = saleRepository.sumCashPaidBetween(startOfDay, endOfDay);
        BigDecimal todayCashBalance = todayCashSales.subtract(todayExpenses);

        long totalProducts = productRepository.count();
        long lowStockProducts = productRepository.countLowStockProducts();
        long totalCustomers = customerRepository.count();

        BigDecimal totalReceivableDue = saleRepository.sumTotalDue();
        BigDecimal totalPayableDue = purchaseRepository.sumTotalDue();

        List<Sale> recentOrders = saleService.getRecentSales();
        List<Object[]> bestSellers = saleService.getBestSellingChocolates(5);

        // Chart Data for the last 7 days
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartSales = new ArrayList<>();
        List<BigDecimal> chartProfits = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("EEE (MMM d)");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            chartLabels.add(date.format(labelFormatter));
            chartSales.add(saleRepository.sumTotalSalesBetween(dayStart, dayEnd));
            chartProfits.add(saleRepository.sumProfitBetween(dayStart, dayEnd));
        }

        return DashboardData.builder()
                .todaySales(todaySales)
                .todayOrders(todayOrders)
                .todayProfit(todayNetProfit)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .totalCustomers(totalCustomers)
                .todayExpenses(todayExpenses)
                .todayCashBalance(todayCashBalance)
                .totalReceivableDue(totalReceivableDue)
                .totalPayableDue(totalPayableDue)
                .recentOrders(recentOrders)
                .bestSellers(bestSellers)
                .chartLabels(chartLabels)
                .chartSales(chartSales)
                .chartProfits(chartProfits)
                .build();
    }
}
