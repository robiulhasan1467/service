package com.chocolateshop.service;

import com.chocolateshop.entity.Customer;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import com.chocolateshop.entity.Supplier;
import com.chocolateshop.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    @Data
    @Builder
    public static class ProfitSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalSales;
        private BigDecimal totalPurchases;
        private BigDecimal grossProfit;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
    }

    @Transactional(readOnly = true)
    public ProfitSummary getProfitSummary(LocalDate start, LocalDate end) {
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.MAX);

        BigDecimal sales = saleRepository.sumTotalSalesBetween(startDT, endDT);
        BigDecimal purchases = purchaseRepository.sumTotalByDateRange(startDT, endDT);
        BigDecimal grossProfit = saleRepository.sumProfitBetween(startDT, endDT);
        BigDecimal expenses = expenseRepository.sumExpensesBetween(start, end);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        return ProfitSummary.builder()
                .startDate(start)
                .endDate(end)
                .totalSales(sales)
                .totalPurchases(purchases)
                .grossProfit(grossProfit)
                .totalExpenses(expenses)
                .netProfit(netProfit)
                .build();
    }

    @Data
    @Builder
    public static class CustomerDueItem {
        private Customer customer;
        private BigDecimal currentDue;
    }

    @Transactional(readOnly = true)
    public List<CustomerDueItem> getCustomerDueReport() {
        List<Customer> customers = customerService.getActiveCustomers();
        List<CustomerDueItem> list = new ArrayList<>();
        for (Customer c : customers) {
            BigDecimal due = customerService.calculateCurrentDue(c.getId());
            if (due.signum() > 0) {
                list.add(CustomerDueItem.builder().customer(c).currentDue(due).build());
            }
        }
        return list;
    }

    @Data
    @Builder
    public static class SupplierDueItem {
        private Supplier supplier;
        private BigDecimal currentDue;
    }

    @Transactional(readOnly = true)
    public List<SupplierDueItem> getSupplierDueReport() {
        List<Supplier> suppliers = supplierService.getActiveSuppliers();
        List<SupplierDueItem> list = new ArrayList<>();
        for (Supplier s : suppliers) {
            BigDecimal due = supplierService.calculateCurrentDue(s.getId());
            if (due.signum() > 0) {
                list.add(SupplierDueItem.builder().supplier(s).currentDue(due).build());
            }
        }
        return list;
    }

    @Data
    @Builder
    public static class InventoryValuation {
        private List<Product> products;
        private BigDecimal totalCostValue;
        private BigDecimal totalRetailValue;
        private BigDecimal potentialProfit;
    }

    @Transactional(readOnly = true)
    public InventoryValuation getInventoryValuationReport() {
        List<Product> products = productRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
        BigDecimal costVal = BigDecimal.ZERO;
        BigDecimal retailVal = BigDecimal.ZERO;

        for (Product p : products) {
            BigDecimal qty = p.getStockQuantity() != null ? p.getStockQuantity() : BigDecimal.ZERO;
            BigDecimal cost = p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal retail = p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO;

            costVal = costVal.add(cost.multiply(qty));
            retailVal = retailVal.add(retail.multiply(qty));
        }

        return InventoryValuation.builder()
                .products(products)
                .totalCostValue(costVal)
                .totalRetailValue(retailVal)
                .potentialProfit(retailVal.subtract(costVal))
                .build();
    }

    /**
     * Generates CSV export for inventory data.
     */
    @Transactional(readOnly = true)
    public String generateInventoryCsv() {
        List<Product> products = productRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Product ID,SKU,Name,Category,Brand,Purchase Cost,Selling Price,Stock Quantity,Min Stock,Status");
        for (Product p : products) {
            pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%s%n",
                    p.getId(),
                    p.getSku(),
                    escapeCsv(p.getName()),
                    p.getCategory() != null ? escapeCsv(p.getCategory().getName()) : "",
                    p.getBrand() != null ? escapeCsv(p.getBrand().getName()) : "",
                    p.getPurchasePrice(),
                    p.getSellingPrice(),
                    p.getStockQuantity(),
                    p.getMinStockLevel(),
                    p.getStatus());
        }
        return sw.toString();
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
