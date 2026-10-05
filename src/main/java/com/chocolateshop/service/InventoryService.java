package com.chocolateshop.service;

import com.chocolateshop.dto.StockAdjustmentDto;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.InventoryTransaction;
import com.chocolateshop.entity.Product;
import com.chocolateshop.repository.InventoryTransactionRepository;
import com.chocolateshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    /**
     * Atomically updates product stock and records an immutable ledger entry.
     */
    @Transactional
    public InventoryTransaction recordMovement(Product product,
                                               Enums.TransactionType type,
                                               BigDecimal quantity,
                                               String reference,
                                               AppUser user,
                                               String note) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        BigDecimal prevStock = product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        BigDecimal newStock;

        switch (type) {
            case PURCHASE:
            case SALE_RETURN:
            case OPENING_STOCK:
                newStock = prevStock.add(quantity);
                break;
            case SALE:
            case PURCHASE_RETURN:
            case DAMAGE:
                if (prevStock.compareTo(quantity) < 0) {
                    throw new IllegalStateException("Insufficient stock for product '" + product.getName() +
                            "'. Available: " + prevStock + ", Requested: " + quantity);
                }
                newStock = prevStock.subtract(quantity);
                break;
            case ADJUSTMENT:
                // For manual stock count reset, quantity represents the target stock count
                newStock = quantity;
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        if (newStock.signum() < 0) {
            throw new IllegalStateException("Stock quantity cannot become negative for product: " + product.getName());
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        InventoryTransaction tx = InventoryTransaction.builder()
                .product(product)
                .transactionType(type)
                .quantity(quantity)
                .previousStock(prevStock)
                .newStock(newStock)
                .reference(reference)
                .transactionDate(LocalDateTime.now())
                .user(user)
                .note(note)
                .build();

        return transactionRepository.save(tx);
    }

    /**
     * Handles manual stock adjustments (damage, recount adjustment, opening stock).
     */
    @Transactional
    public InventoryTransaction adjustStock(StockAdjustmentDto dto, AppUser user) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + dto.getProductId()));

        String ref = "ADJ-" + System.currentTimeMillis();
        return recordMovement(product, dto.getAdjustmentType(), dto.getQuantity(), ref, user, dto.getNote());
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getTransactions(Long productId,
                                                      Enums.TransactionType type,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      Pageable pageable) {
        return transactionRepository.findWithFilters(productId, type, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransaction> getProductHistory(Long productId) {
        return transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }
}
