package com.chocolateshop.service;

import com.chocolateshop.entity.*;
import com.chocolateshop.repository.PurchaseRepository;
import com.chocolateshop.repository.SupplierPaymentRepository;
import com.chocolateshop.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierPaymentRepository supplierPaymentRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers(String query) {
        if (query != null && !query.isBlank()) {
            return supplierRepository.searchSuppliers(query.trim());
        }
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Supplier> getActiveSuppliers() {
        return supplierRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with id: " + id));
    }

    @Transactional
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setStatus(supplier.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateCurrentDue(Long supplierId) {
        Supplier supplier = getSupplierById(supplierId);
        BigDecimal opening = supplier.getOpeningDue() != null ? supplier.getOpeningDue() : BigDecimal.ZERO;

        List<Purchase> purchases = purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(supplierId);
        BigDecimal purchaseDueSum = purchases.stream()
                .filter(p -> p.getStatus() == Enums.PurchaseStatus.CONFIRMED)
                .map(Purchase::getDueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SupplierPayment> payments = supplierPaymentRepository.findBySupplierIdOrderByPaymentDateDesc(supplierId);
        BigDecimal paymentsSum = payments.stream()
                .map(SupplierPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return opening.add(purchaseDueSum).subtract(paymentsSum);
    }

    @Transactional
    public SupplierPayment recordPayment(Long supplierId,
                                         BigDecimal amount,
                                         Enums.PaymentMethod method,
                                         String reference,
                                         String note,
                                         AppUser paidBy) {
        Supplier supplier = getSupplierById(supplierId);

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        SupplierPayment payment = SupplierPayment.builder()
                .supplier(supplier)
                .amount(amount)
                .paymentMethod(method != null ? method : Enums.PaymentMethod.CASH)
                .paymentDate(LocalDateTime.now())
                .reference(reference)
                .note(note)
                .paidBy(paidBy)
                .build();

        return supplierPaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<SupplierPayment> getPaymentHistory(Long supplierId) {
        return supplierPaymentRepository.findBySupplierIdOrderByPaymentDateDesc(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Purchase> getPurchaseHistory(Long supplierId) {
        return purchaseRepository.findBySupplierIdOrderByPurchaseDateDesc(supplierId);
    }
}
