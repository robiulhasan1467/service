package com.chocolateshop.service;

import com.chocolateshop.dto.PurchaseItemDto;
import com.chocolateshop.dto.PurchaseRequest;
import com.chocolateshop.entity.*;
import com.chocolateshop.repository.ProductRepository;
import com.chocolateshop.repository.PurchaseRepository;
import com.chocolateshop.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<Purchase> getPurchases(String query,
                                       Enums.PurchaseStatus status,
                                       LocalDateTime startDate,
                                       LocalDateTime endDate,
                                       Pageable pageable) {
        return purchaseRepository.findWithFilters(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Purchase getPurchaseByInvoiceNo(String invoiceNo) {
        return purchaseRepository.findByInvoiceNo(invoiceNo)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found with invoice: " + invoiceNo));
    }

    /**
     * Creates and confirms a purchase order, automatically increasing inventory and recording transactions.
     */
    @Transactional
    public Purchase createPurchase(PurchaseRequest request, AppUser creator) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with id: " + request.getSupplierId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Purchase order must contain at least one item");
        }

        String invoiceNo = generatePurchaseInvoiceNo();

        Purchase purchase = Purchase.builder()
                .invoiceNo(invoiceNo)
                .supplier(supplier)
                .purchaseDate(LocalDateTime.now())
                .status(Enums.PurchaseStatus.CONFIRMED)
                .notes(request.getNotes())
                .creator(creator)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseItemDto itemDto : request.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemDto.getProductId()));

            BigDecimal lineTotal = itemDto.getUnitPrice().multiply(itemDto.getQuantity());
            totalAmount = totalAmount.add(lineTotal);

            // Update product purchase price to latest cost
            product.setPurchasePrice(itemDto.getUnitPrice());

            PurchaseItem purchaseItem = PurchaseItem.builder()
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .totalPrice(lineTotal)
                    .build();

            purchase.addItem(purchaseItem);

            // Automatically increase inventory and record ledger entry
            inventoryService.recordMovement(
                    product,
                    Enums.TransactionType.PURCHASE,
                    itemDto.getQuantity(),
                    invoiceNo,
                    creator,
                    "Stock replenishment via purchase invoice " + invoiceNo
            );
        }

        BigDecimal paid = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal due = totalAmount.subtract(paid);
        if (due.signum() < 0) {
            due = BigDecimal.ZERO;
        }

        purchase.setTotalAmount(totalAmount);
        purchase.setPaidAmount(paid);
        purchase.setDueAmount(due);

        return purchaseRepository.save(purchase);
    }

    @Transactional
    public void cancelPurchase(Long id, AppUser user) {
        Purchase purchase = getPurchaseById(id);
        if (purchase.getStatus() == Enums.PurchaseStatus.CANCELLED) {
            throw new IllegalStateException("Purchase is already cancelled");
        }

        // Reverse stock increase
        for (PurchaseItem item : purchase.getItems()) {
            inventoryService.recordMovement(
                    item.getProduct(),
                    Enums.TransactionType.PURCHASE_RETURN,
                    item.getQuantity(),
                    "CANCEL-" + purchase.getInvoiceNo(),
                    user,
                    "Cancelled purchase " + purchase.getInvoiceNo()
            );
        }

        purchase.setStatus(Enums.PurchaseStatus.CANCELLED);
        purchaseRepository.save(purchase);
    }

    private String generatePurchaseInvoiceNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(100, 999);
        return "PUR-" + timestamp + "-" + rand;
    }
}
