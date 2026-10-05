package com.chocolateshop.repository;

import com.chocolateshop.entity.Customer;
import com.chocolateshop.entity.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByStatusOrderByNameAsc(Enums.Status status);

    Optional<Customer> findByIsWalkInTrue();

    Optional<Customer> findByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Customer> searchCustomers(@Param("query") String query);
}
