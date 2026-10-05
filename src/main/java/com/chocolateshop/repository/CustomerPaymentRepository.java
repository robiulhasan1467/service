package com.chocolateshop.repository;

import com.chocolateshop.entity.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);
}
