package com.chocolateshop;

import com.chocolateshop.dto.PosCartItem;
import com.chocolateshop.dto.PosPaymentLine;
import com.chocolateshop.dto.PosSaleRequest;
import com.chocolateshop.dto.PurchaseItemDto;
import com.chocolateshop.dto.PurchaseRequest;
import com.chocolateshop.dto.SaleReturnItemDto;
import com.chocolateshop.dto.SaleReturnRequest;
import com.chocolateshop.dto.StockAdjustmentDto;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Customer;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import com.chocolateshop.entity.Purchase;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.entity.SaleReturn;
import com.chocolateshop.entity.Supplier;
import com.chocolateshop.service.CustomerService;
import com.chocolateshop.service.InventoryService;
import com.chocolateshop.service.ProductService;
import com.chocolateshop.service.PurchaseService;
import com.chocolateshop.service.ReportService;
import com.chocolateshop.service.SaleService;
import com.chocolateshop.service.SupplierService;
import com.chocolateshop.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessWorkflowIntegrationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Test
    @Order(1)
    @DisplayName("Verify seed users and default credentials")
    void testSeedUsers() {
        AppUser admin = userService.getByUsername("admin");
        assertNotNull(admin);
        assertEquals(Enums.Role.ADMIN, admin.getRole());
        assertEquals(Enums.Status.ACTIVE, admin.getStatus());

        AppUser cashier = userService.getByUsername("cashier");
        assertNotNull(cashier);
        assertEquals(Enums.Role.CASHIER, cashier.getRole());
    }

    @Test
    @Order(2)
    @DisplayName("Verify seed products and stock levels")
    void testSeedProducts() {
        List<Product> products = productService.getActiveProducts();
        assertFalse(products.isEmpty());
        assertTrue(products.size() >= 10);

        Product first = products.get(0);
        assertNotNull(first.getSku());
        assertNotNull(first.getPurchasePrice());
        assertNotNull(first.getSellingPrice());
        assertTrue(first.getSellingPrice().compareTo(first.getPurchasePrice()) > 0);
    }

    @Test
    @Order(3)
    @DisplayName("Test Purchase Order Workflow: Stock replenishment and inventory ledger audit")
    @Transactional
    void testPurchaseWorkflow() {
        Supplier supplier = supplierService.getActiveSuppliers().get(0);
        Product product = productService.getActiveProducts().get(0);
        BigDecimal initialStock = product.getStockQuantity();

        PurchaseRequest request = new PurchaseRequest();
        request.setSupplierId(supplier.getId());
        request.setPaidAmount(new BigDecimal("100.00"));
        request.setNotes("Test stock procurement");

        PurchaseItemDto item = new PurchaseItemDto();
        item.setProductId(product.getId());
        item.setQuantity(new BigDecimal("10.00"));
        item.setUnitPrice(new BigDecimal("15.00"));
        request.setItems(List.of(item));

        AppUser admin = userService.getByUsername("admin");
        Purchase purchase = purchaseService.createPurchase(request, admin);

        assertNotNull(purchase.getId());
        assertEquals(Enums.PurchaseStatus.CONFIRMED, purchase.getStatus());

        // Verify stock incremented by 10
        Product updatedProduct = productService.getProductById(product.getId());
        assertEquals(initialStock.add(new BigDecimal("10.00")), updatedProduct.getStockQuantity());
    }

    @Test
    @Order(4)
    @DisplayName("Test POS Checkout Workflow: Real-time stock decrement, ledger entry, and sales order")
    @Transactional
    void testPosCheckoutWorkflow() {
        Customer customer = customerService.getActiveCustomers().get(0);
        Product product = productService.getActiveProducts().get(0);
        BigDecimal initialStock = product.getStockQuantity();

        PosSaleRequest posRequest = new PosSaleRequest();
        posRequest.setCustomerId(customer.getId());
        posRequest.setDiscount(BigDecimal.ZERO);
        posRequest.setTax(BigDecimal.ZERO);

        PosCartItem cartItem = new PosCartItem();
        cartItem.setProductId(product.getId());
        cartItem.setQuantity(new BigDecimal("2.00"));
        cartItem.setUnitPrice(product.getSellingPrice());
        cartItem.setDiscount(BigDecimal.ZERO);
        posRequest.setItems(List.of(cartItem));

        BigDecimal subtotal = product.getSellingPrice().multiply(new BigDecimal("2.00"));
        posRequest.setPaidAmount(subtotal);
        posRequest.setPaymentMethod(Enums.PaymentMethod.CASH);

        PosPaymentLine payment = new PosPaymentLine();
        payment.setMethod(Enums.PaymentMethod.CASH);
        payment.setAmount(subtotal);
        posRequest.setPayments(List.of(payment));

        AppUser cashier = userService.getByUsername("cashier");
        Sale sale = saleService.createSale(posRequest, cashier);

        assertNotNull(sale.getId());
        assertNotNull(sale.getInvoiceNo());
        assertEquals(Enums.SaleStatus.COMPLETED, sale.getStatus());

        // Verify stock decremented by 2
        Product updatedProduct = productService.getProductById(product.getId());
        assertEquals(initialStock.subtract(new BigDecimal("2.00")), updatedProduct.getStockQuantity());
    }

    @Test
    @Order(5)
    @DisplayName("Test Sales Return: Product return, stock reversal, and profit adjustment")
    @Transactional
    void testSalesReturnWorkflow() {
        // Find existing sale
        List<Sale> sales = saleService.getRecentSales();
        assertFalse(sales.isEmpty());
        Sale sale = sales.get(0);
        assertFalse(sale.getItems().isEmpty());

        var firstItem = sale.getItems().get(0);
        Product product = firstItem.getProduct();
        BigDecimal initialStock = productService.getProductById(product.getId()).getStockQuantity();

        SaleReturnRequest returnReq = new SaleReturnRequest();
        returnReq.setSaleId(sale.getId());
        returnReq.setReason("Customer changed mind on gift wrapping");

        SaleReturnItemDto returnItem = new SaleReturnItemDto();
        returnItem.setSaleItemId(firstItem.getId());
        returnItem.setReturnQuantity(new BigDecimal("1.00"));
        returnReq.setItems(List.of(returnItem));

        AppUser admin = userService.getByUsername("admin");
        SaleReturn saleReturn = saleService.processReturn(returnReq, admin);

        assertNotNull(saleReturn.getId());
        assertNotNull(saleReturn.getReturnNo());
        assertTrue(saleReturn.getRefundAmount().compareTo(BigDecimal.ZERO) > 0);

        // Verify stock reversed (incremented by 1)
        Product restoredProduct = productService.getProductById(product.getId());
        assertEquals(initialStock.add(new BigDecimal("1.00")), restoredProduct.getStockQuantity());
    }

    @Test
    @Order(6)
    @DisplayName("Test Manual Stock Recount and Damage Write-Off")
    @Transactional
    void testInventoryAdjustment() {
        Product product = productService.getActiveProducts().get(0);
        AppUser admin = userService.getByUsername("admin");

        // 1. Recount adjustment: sets stock to specific quantity
        StockAdjustmentDto recount = new StockAdjustmentDto();
        recount.setProductId(product.getId());
        recount.setAdjustmentType(Enums.TransactionType.ADJUSTMENT);
        recount.setQuantity(new BigDecimal("25.00"));
        recount.setNote("Physical store audit recount");

        inventoryService.adjustStock(recount, admin);
        Product afterRecount = productService.getProductById(product.getId());
        assertEquals(new BigDecimal("25.00"), afterRecount.getStockQuantity());

        // 2. Damage write-off: decreases stock
        StockAdjustmentDto damage = new StockAdjustmentDto();
        damage.setProductId(product.getId());
        damage.setAdjustmentType(Enums.TransactionType.DAMAGE);
        damage.setQuantity(new BigDecimal("2.00"));
        damage.setNote("Heat damage write-off");

        inventoryService.adjustStock(damage, admin);
        Product afterDamage = productService.getProductById(product.getId());
        assertEquals(new BigDecimal("23.00"), afterDamage.getStockQuantity());
    }

    @Test
    @Order(7)
    @DisplayName("Test Reports Generation: Profit & Loss and Inventory Valuation")
    void testReports() {
        ReportService.ProfitSummary profitSummary = reportService.getProfitSummary(
                LocalDate.now().minusDays(30), LocalDate.now()
        );
        assertNotNull(profitSummary);
        assertNotNull(profitSummary.getTotalSales());
        assertNotNull(profitSummary.getGrossProfit());
        assertNotNull(profitSummary.getNetProfit());

        ReportService.InventoryValuation valuation = reportService.getInventoryValuationReport();
        assertNotNull(valuation);
        assertFalse(valuation.getProducts().isEmpty());
        assertTrue(valuation.getTotalCostValue().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(valuation.getTotalRetailValue().compareTo(BigDecimal.ZERO) > 0);

        String csv = reportService.generateInventoryCsv();
        assertNotNull(csv);
        assertTrue(csv.contains("SKU,Name,Category"));
    }
}
