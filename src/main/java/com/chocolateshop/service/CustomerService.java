package com.chocolateshop.service;

import com.chocolateshop.entity.*;
import com.chocolateshop.repository.CustomerPaymentRepository;
import com.chocolateshop.repository.CustomerRepository;
import com.chocolateshop.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerPaymentRepository customerPaymentRepository;
    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers(String query) {
        if (query != null && !query.isBlank()) {
            return customerRepository.searchCustomers(query.trim());
        }
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> getActiveCustomers() {
        return customerRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + id));
    }

    @Transactional
    public Customer getOrCreateWalkInCustomer() {
        return customerRepository.findByIsWalkInTrue().orElseGet(() -> {
            Customer walkIn = Customer.builder()
                    .name("Walk-in Customer")
                    .phone("N/A")
                    .email("walkin@chocolateshop.local")
                    .address("Retail Counter")
                    .openingDue(BigDecimal.ZERO)
                    .isWalkIn(true)
                    .status(Enums.Status.ACTIVE)
                    .build();
            return customerRepository.save(walkIn);
        });
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer.getPhone() != null && !customer.getPhone().isBlank() && !customer.getPhone().equalsIgnoreCase("N/A")) {
            customerRepository.findByPhone(customer.getPhone()).ifPresent(existing -> {
                if (customer.getId() == null || !existing.getId().equals(customer.getId())) {
                    throw new IllegalArgumentException("Customer with phone '" + customer.getPhone() + "' already exists");
                }
            });
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Customer customer = getCustomerById(id);
        customer.setStatus(customer.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateCurrentDue(Long customerId) {
        Customer customer = getCustomerById(customerId);
        BigDecimal opening = customer.getOpeningDue() != null ? customer.getOpeningDue() : BigDecimal.ZERO;

        List<Sale> sales = saleRepository.findByCustomerIdOrderBySaleDateDesc(customerId);
        BigDecimal salesDueSum = sales.stream()
                .filter(s -> s.getStatus() != Enums.SaleStatus.CANCELLED)
                .map(Sale::getDueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CustomerPayment> payments = customerPaymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);
        BigDecimal paymentsSum = payments.stream()
                .map(CustomerPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return opening.add(salesDueSum).subtract(paymentsSum);
    }

    @Transactional
    public CustomerPayment recordPayment(Long customerId,
                                         BigDecimal amount,
                                         Enums.PaymentMethod method,
                                         String reference,
                                         String note,
                                         AppUser receivedBy) {
        Customer customer = getCustomerById(customerId);

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        CustomerPayment payment = CustomerPayment.builder()
                .customer(customer)
                .amount(amount)
                .paymentMethod(method != null ? method : Enums.PaymentMethod.CASH)
                .paymentDate(LocalDateTime.now())
                .reference(reference)
                .note(note)
                .receivedBy(receivedBy)
                .build();

        return customerPaymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<CustomerPayment> getPaymentHistory(Long customerId) {
        return customerPaymentRepository.findByCustomerIdOrderByPaymentDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Sale> getPurchaseHistory(Long customerId) {
        return saleRepository.findByCustomerIdOrderBySaleDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public long countTotalCustomers() {
        return customerRepository.count();
    }
}
