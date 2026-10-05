package com.chocolateshop.repository;

import com.chocolateshop.entity.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    List<SupplierPayment> findBySupplierIdOrderByPaymentDateDesc(Long supplierId);
}
