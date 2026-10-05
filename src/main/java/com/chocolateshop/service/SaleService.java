package com.chocolateshop.service;

import com.chocolateshop.dto.*;
import com.chocolateshop.entity.*;
import com.chocolateshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<Sale> getSales(String query,
                               Enums.SaleStatus status,
                               LocalDateTime startDate,
                               LocalDateTime endDate,
                               Pageable pageable) {
        return saleRepository.findWithFilters(query, status, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Sale> getSalesWithDelivery(String query,
                                           Enums.SaleStatus status,
                                           Enums.DeliveryStatus deliveryStatus,
                                           LocalDateTime startDate,
                                           LocalDateTime endDate,
                                           Pageable pageable) {
        return saleRepository.findWithDeliveryFilters(query, status, deliveryStatus, startDate, endDate, pageable);
    }

    @Transactional
    public Sale updateDeliveryStatus(Long saleId, Enums.DeliveryStatus deliveryStatus, String courierName, String trackingCode) {
        Sale sale = getSaleById(saleId);
        sale.setDeliveryStatus(deliveryStatus);
        if (courierName != null && !courierName.isBlank()) {
            sale.setCourierName(courierName.trim());
        }
        if (trackingCode != null && !trackingCode.isBlank()) {
            sale.setTrackingCode(trackingCode.trim());
        }
        // If order marked DELIVERED, COD balance has been collected!
        if (deliveryStatus == Enums.DeliveryStatus.DELIVERED) {
            sale.setPaidAmount(sale.getTotalAmount());
            sale.setDueAmount(BigDecimal.ZERO);
        }
        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public Sale getSaleById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Sale getSaleByInvoiceNo(String invoiceNo) {
        return saleRepository.findByInvoiceNo(invoiceNo)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found with invoice: " + invoiceNo));
    }

    @Transactional(readOnly = true)
    public List<Sale> getRecentSales() {
        return saleRepository.findTop10ByOrderBySaleDateDesc();
    }

    /**
     * Completes a POS sale: validates stock, calculates profit, creates invoice,
     * decreases inventory atomically, and saves sale payments.
     */
    @Transactional
    public Sale createSale(PosSaleRequest request, AppUser cashier) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty");
        }

        Customer customer;
        if (request.getCustomerId() != null) {
            customer = customerService.getCustomerById(request.getCustomerId());
        } else {
            customer = customerService.getOrCreateWalkInCustomer();
        }

        String invoiceNo = generateSaleInvoiceNo();

        Sale sale = Sale.builder()
                .invoiceNo(invoiceNo)
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .cashier(cashier)
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : Enums.PaymentMethod.CASH)
                .status(Enums.SaleStatus.COMPLETED)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (PosCartItem cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + cartItem.getProductId()));

            // Stock availability check
            if (product.getStockQuantity() == null || product.getStockQuantity().compareTo(cartItem.getQuantity()) < 0) {
                throw new IllegalStateException("Insufficient stock for product '" + product.getName() +
                        "'. In stock: " + product.getStockQuantity() + ", requested: " + cartItem.getQuantity());
            }

            BigDecimal itemDiscount = cartItem.getDiscount() != null ? cartItem.getDiscount() : BigDecimal.ZERO;
            BigDecimal lineTotal = cartItem.getUnitPrice().multiply(cartItem.getQuantity()).subtract(itemDiscount);
            if (lineTotal.signum() < 0) {
                lineTotal = BigDecimal.ZERO;
            }

            BigDecimal purchaseCost = product.getPurchasePrice() != null ? product.getPurchasePrice() : BigDecimal.ZERO;
            // Profit calculation: (Selling Price - Purchase Cost) * Qty - Item Discount
            BigDecimal lineProfit = cartItem.getUnitPrice().subtract(purchaseCost)
                    .multiply(cartItem.getQuantity())
                    .subtract(itemDiscount);

            subtotal = subtotal.add(lineTotal);
            totalProfit = totalProfit.add(lineProfit);

            SaleItem saleItem = SaleItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .returnedQuantity(BigDecimal.ZERO)
                    .unitPrice(cartItem.getUnitPrice())
                    .purchaseCost(purchaseCost)
                    .discount(itemDiscount)
                    .totalPrice(lineTotal)
                    .profit(lineProfit)
                    .build();

            sale.addItem(saleItem);

            // Deduct inventory and log ledger
            inventoryService.recordMovement(
                    product,
                    Enums.TransactionType.SALE,
                    cartItem.getQuantity(),
                    invoiceNo,
                    cashier,
                    "Retail sale invoice " + invoiceNo
            );
        }

        BigDecimal generalDiscount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.subtract(generalDiscount).add(tax);
        if (grandTotal.signum() < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        // Adjust total profit for general invoice discount
        totalProfit = totalProfit.subtract(generalDiscount);

        BigDecimal paid = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal due = BigDecimal.ZERO;
        BigDecimal change = BigDecimal.ZERO;

        if (paid.compareTo(grandTotal) >= 0) {
            change = paid.subtract(grandTotal);
            due = BigDecimal.ZERO;
        } else {
            due = grandTotal.subtract(paid);
            change = BigDecimal.ZERO;
        }

        sale.setSubtotal(subtotal);
        sale.setDiscount(generalDiscount);
        sale.setTax(tax);
        sale.setTotalAmount(grandTotal);
        sale.setPaidAmount(paid);
        sale.setDueAmount(due);
        sale.setChangeAmount(change);
        sale.setProfit(totalProfit);

        // Record split / tender payments if provided
        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (PosPaymentLine line : request.getPayments()) {
                SalePayment payment = SalePayment.builder()
                        .paymentMethod(line.getMethod())
                        .amount(line.getAmount())
                        .transactionRef(line.getReference())
                        .build();
                sale.addPayment(payment);
                if (sb.length() > 0) sb.append(", ");
                sb.append(line.getMethod()).append(": $").append(line.getAmount());
            }
            sale.setPaymentDetails(sb.toString());
            sale.setPaymentMethod(Enums.PaymentMethod.MIXED);
        } else {
            SalePayment singlePayment = SalePayment.builder()
                    .paymentMethod(sale.getPaymentMethod())
                    .amount(paid)
                    .build();
            sale.addPayment(singlePayment);
            sale.setPaymentDetails(sale.getPaymentMethod() + ": $" + paid);
        }

        return saleRepository.save(sale);
    }

    /**
     * Processes customer sales return: validates returnable quantity, increases stock,
     * reverses proportional profit, and updates Sale record without deleting history.
     */
    @Transactional
    public SaleReturn processReturn(SaleReturnRequest request, AppUser user) {
        Sale sale = getSaleById(request.getSaleId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Return request must contain items");
        }

        String returnNo = "RET-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" +
                ThreadLocalRandom.current().nextInt(100, 999);

        SaleReturn saleReturn = SaleReturn.builder()
                .returnNo(returnNo)
                .sale(sale)
                .returnDate(LocalDateTime.now())
                .reason(request.getReason())
                .processedBy(user)
                .build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        BigDecimal totalReversedProfit = BigDecimal.ZERO;

        for (SaleReturnItemDto retDto : request.getItems()) {
            SaleItem saleItem = saleItemRepository.findById(retDto.getSaleItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Sale item not found with id: " + retDto.getSaleItemId()));

            BigDecimal available = saleItem.getAvailableToReturn();
            if (retDto.getReturnQuantity().compareTo(available) > 0) {
                throw new IllegalStateException("Return quantity (" + retDto.getReturnQuantity() +
                        ") exceeds available quantity (" + available + ") for " + saleItem.getProduct().getName());
            }

            BigDecimal itemRefund = saleItem.getUnitPrice().multiply(retDto.getReturnQuantity());
            // Reversal of profit: (unitPrice - purchaseCost) * returnQty
            BigDecimal reversedProfit = saleItem.getUnitPrice().subtract(saleItem.getPurchaseCost())
                    .multiply(retDto.getReturnQuantity());

            totalRefund = totalRefund.add(itemRefund);
            totalReversedProfit = totalReversedProfit.add(reversedProfit);

            // Update item's returned quantity
            saleItem.setReturnedQuantity(saleItem.getReturnedQuantity().add(retDto.getReturnQuantity()));
            saleItemRepository.save(saleItem);

            SaleReturnItem returnItem = SaleReturnItem.builder()
                    .saleItem(saleItem)
                    .product(saleItem.getProduct())
                    .quantity(retDto.getReturnQuantity())
                    .unitPrice(saleItem.getUnitPrice())
                    .refundTotal(itemRefund)
                    .reversedProfit(reversedProfit)
                    .build();

            saleReturn.addItem(returnItem);

            // Increase inventory and record ledger entry
            inventoryService.recordMovement(
                    saleItem.getProduct(),
                    Enums.TransactionType.SALE_RETURN,
                    retDto.getReturnQuantity(),
                    returnNo,
                    user,
                    "Sales return for invoice " + sale.getInvoiceNo() + ". Reason: " + request.getReason()
            );
        }

        saleReturn.setRefundAmount(totalRefund);

        // Adjust original sale's recorded profit and status
        sale.setProfit(sale.getProfit().subtract(totalReversedProfit));

        // Check if fully returned
        boolean allReturned = sale.getItems().stream()
                .allMatch(item -> item.getAvailableToReturn().compareTo(BigDecimal.ZERO) == 0);

        sale.setStatus(allReturned ? Enums.SaleStatus.RETURNED : Enums.SaleStatus.PARTIALLY_RETURNED);
        saleRepository.save(sale);

        return saleReturnRepository.save(saleReturn);
    }

    @Transactional(readOnly = true)
    public List<SaleReturn> getReturnsForSale(Long saleId) {
        return saleReturnRepository.findBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public Page<SaleReturn> getReturns(String query, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return saleReturnRepository.findWithFilters(query, startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getBestSellingChocolates(int limit) {
        return saleItemRepository.findBestSellingProducts(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesByCustomerPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return List.of();
        }
        return saleRepository.findByCustomerPhone(phone.trim());
    }

    /**
     * Creates an online quick order from the JaHa Live Shopping storefront ("অর্ডার করুন").
     * Decrements inventory atomically and generates an invoice number.
     */
    @Transactional
    public Sale createOnlineOrder(QuickOrderRequest request) {
        if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
            throw new IllegalArgumentException("Customer mobile number is required");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        // 1. Resolve or register customer
        String cleanPhone = request.getCustomerPhone().trim();
        Customer customer = customerRepository.findByPhone(cleanPhone)
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .name(request.getCustomerName().trim())
                        .phone(cleanPhone)
                        .address(request.getDeliveryAddress().trim())
                        .openingDue(BigDecimal.ZERO)
                        .isWalkIn(false)
                        .status(Enums.Status.ACTIVE)
                        .build()));

        if (request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()) {
            customer.setAddress(request.getDeliveryAddress().trim());
            customerRepository.save(customer);
        }

        // 2. Generate JaHa branded invoice number
        String invoiceNo = generateJahaInvoiceNo();

        // 3. Payment Method
        Enums.PaymentMethod paymentMethod = Enums.PaymentMethod.CASH;
        if ("BKASH".equalsIgnoreCase(request.getPaymentMethod())) {
            paymentMethod = Enums.PaymentMethod.BKASH;
        } else if ("NAGAD".equalsIgnoreCase(request.getPaymentMethod())) {
            paymentMethod = Enums.PaymentMethod.NAGAD;
        }

        // 4. Delivery charge: Inside Dhaka ৳60, Outside Dhaka ৳120
        BigDecimal deliveryFee = "OUTSIDE".equalsIgnoreCase(request.getDeliveryZone()) 
                ? new BigDecimal("120.00") 
                : new BigDecimal("60.00");

        Sale sale = Sale.builder()
                .invoiceNo(invoiceNo)
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .cashier(null)
                .paymentMethod(paymentMethod)
                .paymentDetails("Zone: " + ("OUTSIDE".equalsIgnoreCase(request.getDeliveryZone()) ? "Outside Dhaka (৳120)" : "Inside Dhaka (৳60)") + " | Delivery Address: " + request.getDeliveryAddress())
                .notes("JaHa Live Shopping Online Order. " + (request.getNotes() != null ? request.getNotes() : ""))
                .status(Enums.SaleStatus.COMPLETED)
                .tax(deliveryFee) // Store delivery fee in tax/shipping field
                .discount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        // Process order items
        List<QuickOrderRequest.CartItemRequest> itemsToProcess = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            itemsToProcess.addAll(request.getItems());
        } else if (request.getProductId() != null) {
            BigDecimal qty = request.getQuantity() != null && request.getQuantity() > 0 
                    ? new BigDecimal(request.getQuantity()) 
                    : BigDecimal.ONE;
            itemsToProcess.add(new QuickOrderRequest.CartItemRequest(request.getProductId(), qty));
        } else {
            throw new IllegalArgumentException("No product specified for order");
        }

        for (QuickOrderRequest.CartItemRequest itemReq : itemsToProcess) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemReq.getProductId()));

            BigDecimal qty = itemReq.getQuantity() != null && itemReq.getQuantity().signum() > 0 
                    ? itemReq.getQuantity() 
                    : BigDecimal.ONE;

            // Stock check
            if (product.getStockQuantity() == null || product.getStockQuantity().compareTo(qty) < 0) {
                throw new IllegalStateException("দুঃখিত, '" + product.getName() + "' পণ্যটি পর্যাপ্ত পরিমাণে স্টকে নেই। বর্তমান স্টক: " + product.getStockQuantity());
            }

            BigDecimal unitPrice = product.getEffectivePrice();
            BigDecimal lineTotal = unitPrice.multiply(qty);
            BigDecimal purchaseCost = product.getPurchasePrice() != null ? product.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal lineProfit = unitPrice.subtract(purchaseCost).multiply(qty);

            subtotal = subtotal.add(lineTotal);
            totalProfit = totalProfit.add(lineProfit);

            SaleItem saleItem = SaleItem.builder()
                    .product(product)
                    .quantity(qty)
                    .returnedQuantity(BigDecimal.ZERO)
                    .unitPrice(unitPrice)
                    .purchaseCost(purchaseCost)
                    .discount(BigDecimal.ZERO)
                    .totalPrice(lineTotal)
                    .profit(lineProfit)
                    .build();

            sale.addItem(saleItem);

            // Decrement inventory
            inventoryService.recordMovement(
                    product,
                    Enums.TransactionType.SALE,
                    qty,
                    invoiceNo,
                    null,
                    "JaHa Online Order " + invoiceNo
            );
        }

        BigDecimal grandTotal = subtotal.add(deliveryFee);
        sale.setSubtotal(subtotal);
        sale.setTotalAmount(grandTotal);
        // For COD (Cash on Delivery), paid is 0, due is grandTotal
        sale.setPaidAmount(BigDecimal.ZERO);
        sale.setDueAmount(grandTotal);
        sale.setChangeAmount(BigDecimal.ZERO);
        sale.setProfit(totalProfit);

        return saleRepository.save(sale);
    }

    private String generateJahaInvoiceNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "JH-" + datePart + "-" + rand;
    }

    private String generateSaleInvoiceNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "INV-" + timestamp + "-" + rand;
    }
}
