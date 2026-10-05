
package com.chocolateshop.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chocolateshop.dto.PosCartItem;
import com.chocolateshop.dto.PosSaleRequest;
import com.chocolateshop.dto.PurchaseItemDto;
import com.chocolateshop.dto.PurchaseRequest;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Brand;
import com.chocolateshop.entity.Category;
import com.chocolateshop.entity.Customer;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Expense;
import com.chocolateshop.entity.ExpenseCategory;
import com.chocolateshop.entity.Product;
import com.chocolateshop.entity.Supplier;
import com.chocolateshop.repository.BrandRepository;
import com.chocolateshop.repository.CategoryRepository;
import com.chocolateshop.repository.CustomerRepository;
import com.chocolateshop.repository.ExpenseCategoryRepository;
import com.chocolateshop.repository.ExpenseRepository;
import com.chocolateshop.repository.ProductRepository;
import com.chocolateshop.repository.SupplierRepository;
import com.chocolateshop.repository.UserRepository;
import com.chocolateshop.service.PurchaseService;
import com.chocolateshop.service.SaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializes database with complete, production-realistic seed data on initial startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseRepository expenseRepository;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized with data. Skipping seed.");
            return;
        }

        log.info("Starting seed data initialization for Chocolate Shop Management System...");

        // 1. Seed Users
        AppUser admin = userRepository.save(AppUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .email("admin@chocolateshop.local")
                .phone("+1-555-0101")
                .role(Enums.Role.ADMIN)
                .status(Enums.Status.ACTIVE)
                .build());

        AppUser manager = userRepository.save(AppUser.builder()
                .username("manager")
                .password(passwordEncoder.encode("Manager@123"))
                .fullName("Store Operations Manager")
                .email("manager@chocolateshop.local")
                .phone("+1-555-0102")
                .role(Enums.Role.MANAGER)
                .status(Enums.Status.ACTIVE)
                .build());

        AppUser cashier = userRepository.save(AppUser.builder()
                .username("cashier")
                .password(passwordEncoder.encode("Cashier@123"))
                .fullName("Alice Cashier")
                .email("cashier@chocolateshop.local")
                .phone("+1-555-0103")
                .role(Enums.Role.CASHIER)
                .status(Enums.Status.ACTIVE)
                .build());

        AppUser inventoryMgr = userRepository.save(AppUser.builder()
                .username("inventory")
                .password(passwordEncoder.encode("Inventory@123"))
                .fullName("Bob Inventory Lead")
                .email("inventory@chocolateshop.local")
                .phone("+1-555-0104")
                .role(Enums.Role.INVENTORY_MANAGER)
                .status(Enums.Status.ACTIVE)
                .build());

        // 2. Seed Categories
        Category milkCat = categoryRepository.save(Category.builder().name("Milk Chocolate").description("Creamy, rich milk chocolate bars and bites").build());
        Category darkCat = categoryRepository.save(Category.builder().name("Dark Chocolate").description("High cocoa content artisan dark chocolates").build());
        Category whiteCat = categoryRepository.save(Category.builder().name("White Chocolate").description("Velvety cocoa butter white chocolate confections").build());
        Category giftCat = categoryRepository.save(Category.builder().name("Gift Box").description("Curated luxury chocolate gift hampers and boxes").build());
        Category handmadeCat = categoryRepository.save(Category.builder().name("Handmade").description("Freshly crafted artisan pralines and truffles").build());
        Category premiumCat = categoryRepository.save(Category.builder().name("Premium").description("Imported single-origin gourmet chocolates").build());
        Category kidsCat = categoryRepository.save(Category.builder().name("Kids").description("Fun chocolate surprises, eggs, and wafers").build());
        Category seasonalCat = categoryRepository.save(Category.builder().name("Seasonal").description("Limited seasonal release batches and ruby cocoa").build());

        // 3. Seed Brands
        Brand cadbury = brandRepository.save(Brand.builder().name("Cadbury").description("World renowned classic British confectionery").build());
        Brand nestle = brandRepository.save(Brand.builder().name("Nestle").description("Swiss multinational food and chocolate drink brand").build());
        Brand ferrero = brandRepository.save(Brand.builder().name("Ferrero").description("Italian luxury hazelnut chocolate masters").build());
        Brand lindt = brandRepository.save(Brand.builder().name("Lindt & Sprüngli").description("Master Swiss chocolatiers since 1845").build());
        Brand toblerone = brandRepository.save(Brand.builder().name("Toblerone").description("Distinctive triangular Swiss honey and almond nougat").build());
        Brand maison = brandRepository.save(Brand.builder().name("Artisanal House").description("In-house hand-crafted chocolate confectioneries").build());

        // 4. Seed Products (16 items)
        List<Product> products = Arrays.asList(
                Product.builder().sku("CHK-CAD-001").name("Dairy Milk Silk").category(milkCat).brand(cadbury)
                        .description("Smooth and creamy melt-in-mouth milk chocolate bar")
                        .purchasePrice(new BigDecimal("1.80")).sellingPrice(new BigDecimal("2.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("10.00")).unit("bar").weight(new BigDecimal("150")).expiryDate(LocalDate.now().plusMonths(8)).build(),

                Product.builder().sku("CHK-NES-002").name("KitKat 4-Finger").category(milkCat).brand(nestle)
                        .description("Crisp wafer fingers covered in smooth milk chocolate")
                        .purchasePrice(new BigDecimal("0.75")).sellingPrice(new BigDecimal("1.49"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("15.00")).unit("bar").weight(new BigDecimal("45")).expiryDate(LocalDate.now().plusMonths(6)).build(),

                Product.builder().sku("CHK-SNK-003").name("Snickers Bar").category(milkCat).brand(maison)
                        .description("Milk chocolate packed with roasted peanuts, nougat and caramel")
                        .purchasePrice(new BigDecimal("0.90")).sellingPrice(new BigDecimal("1.79"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("10.00")).unit("bar").weight(new BigDecimal("50")).expiryDate(LocalDate.now().plusMonths(7)).build(),

                Product.builder().sku("CHK-FER-004").name("Ferrero Rocher 16-pc").category(premiumCat).brand(ferrero)
                        .description("Whole hazelnut dipped in smooth cocoa cream within a crunchy wafer")
                        .purchasePrice(new BigDecimal("6.50")).sellingPrice(new BigDecimal("10.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("8.00")).unit("box").weight(new BigDecimal("200")).expiryDate(LocalDate.now().plusMonths(9)).build(),

                Product.builder().sku("CHK-LND-005").name("Lindt Excellence 85% Dark").category(darkCat).brand(lindt)
                        .description("Deep, intense full-bodied dark chocolate for true cocoa connoisseurs")
                        .purchasePrice(new BigDecimal("2.80")).sellingPrice(new BigDecimal("4.49"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("8.00")).unit("bar").weight(new BigDecimal("100")).expiryDate(LocalDate.now().plusMonths(12)).build(),

                Product.builder().sku("CHK-LND-006").name("Lindt Swiss Classic White").category(whiteCat).brand(lindt)
                        .description("Extra fine white chocolate made with real vanilla and milk cream")
                        .purchasePrice(new BigDecimal("2.50")).sellingPrice(new BigDecimal("3.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("6.00")).unit("bar").weight(new BigDecimal("100")).expiryDate(LocalDate.now().plusMonths(10)).build(),

                Product.builder().sku("CHK-TOB-007").name("Toblerone Milk 100g").category(milkCat).brand(toblerone)
                        .description("Iconic Swiss milk chocolate with honey and almond nougat peaks")
                        .purchasePrice(new BigDecimal("1.70")).sellingPrice(new BigDecimal("2.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("12.00")).unit("bar").weight(new BigDecimal("100")).expiryDate(LocalDate.now().plusMonths(11)).build(),

                Product.builder().sku("CHK-TOB-008").name("Toblerone Dark 100g").category(darkCat).brand(toblerone)
                        .description("Rich bittersweet dark chocolate with honey nougat pieces")
                        .purchasePrice(new BigDecimal("1.90")).sellingPrice(new BigDecimal("3.29"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("8.00")).unit("bar").weight(new BigDecimal("100")).expiryDate(LocalDate.now().plusMonths(11)).build(),

                Product.builder().sku("CHK-MSN-009").name("Artisan Dark Truffles Box").category(handmadeCat).brand(maison)
                        .description("Handmade 70% Ecuador dark cocoa ganache rolled in bitter cocoa powder")
                        .purchasePrice(new BigDecimal("8.00")).sellingPrice(new BigDecimal("14.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("5.00")).unit("box").weight(new BigDecimal("220")).expiryDate(LocalDate.now().plusMonths(2)).build(),

                Product.builder().sku("CHK-MSN-010").name("Salted Caramel Pralines").category(handmadeCat).brand(maison)
                        .description("Crisp chocolate shells filled with slow-cooked Brittany sea salt caramel")
                        .purchasePrice(new BigDecimal("7.00")).sellingPrice(new BigDecimal("12.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("5.00")).unit("box").weight(new BigDecimal("180")).expiryDate(LocalDate.now().plusMonths(2)).build(),

                Product.builder().sku("CHK-MSN-011").name("Belgian Hazelnut Crunch").category(premiumCat).brand(maison)
                        .description("Roasted Piedmont hazelnuts enrobed in Belgian dark chocolate")
                        .purchasePrice(new BigDecimal("4.50")).sellingPrice(new BigDecimal("7.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("6.00")).unit("pack").weight(new BigDecimal("150")).expiryDate(LocalDate.now().plusMonths(5)).build(),

                Product.builder().sku("CHK-MSN-012").name("Ruby Cocoa Luxury Bar").category(seasonalCat).brand(maison)
                        .description("Naturally pink ruby cocoa beans with luscious berry fruitiness")
                        .purchasePrice(new BigDecimal("3.20")).sellingPrice(new BigDecimal("5.49"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("4.00")).unit("bar").weight(new BigDecimal("90")).expiryDate(LocalDate.now().plusMonths(4)).build(),

                Product.builder().sku("CHK-KID-013").name("Kinder Joy Box (3-Pack)").category(kidsCat).brand(ferrero)
                        .description("Milky and cocoa creams with two crispy wafer balls and surprise toy")
                        .purchasePrice(new BigDecimal("2.20")).sellingPrice(new BigDecimal("3.89"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("10.00")).unit("box").weight(new BigDecimal("60")).expiryDate(LocalDate.now().plusMonths(6)).build(),

                Product.builder().sku("CHK-GFT-014").name("Royal Confectionery Gift Box").category(giftCat).brand(ferrero)
                        .description("Golden prestige collection featuring dark, milk and praline selections")
                        .purchasePrice(new BigDecimal("14.00")).sellingPrice(new BigDecimal("24.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("4.00")).unit("box").weight(new BigDecimal("400")).expiryDate(LocalDate.now().plusMonths(8)).build(),

                Product.builder().sku("CHK-ALM-015").name("Cocoa Dusted Almond Bites").category(darkCat).brand(maison)
                        .description("Caramelized whole California almonds enveloped in dark cocoa")
                        .purchasePrice(new BigDecimal("3.50")).sellingPrice(new BigDecimal("5.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("8.00")).unit("pack").weight(new BigDecimal("120")).expiryDate(LocalDate.now().plusMonths(6)).build(),

                Product.builder().sku("CHK-MAT-016").name("Matcha Green Tea White Bar").category(seasonalCat).brand(maison)
                        .description("Ceremonial grade Uji Japanese matcha blended with white chocolate")
                        .purchasePrice(new BigDecimal("3.60")).sellingPrice(new BigDecimal("5.99"))
                        .stockQuantity(BigDecimal.ZERO).minStockLevel(new BigDecimal("5.00")).unit("bar").weight(new BigDecimal("85")).expiryDate(LocalDate.now().plusMonths(4)).build()
        );

        for (Product p : products) {
            productRepository.save(p);
        }

        // 5. Seed Suppliers
        Supplier swissSupplier = supplierRepository.save(Supplier.builder()
                .name("Hans Peter")
                .companyName("Swiss Cocoa Importers Ltd")
                .phone("+41-22-555-1234")
                .email("orders@swisscocoa.ch")
                .address("Bahnhofstrasse 42, Zurich, Switzerland")
                .openingDue(BigDecimal.ZERO)
                .status(Enums.Status.ACTIVE)
                .build());

        Supplier localSupplier = supplierRepository.save(Supplier.builder()
                .name("Robert Clark")
                .companyName("Gold Star Packaging & Ingredients")
                .phone("+1-555-987-6543")
                .email("clark@goldstarpackaging.com")
                .address("742 Industrial Parkway, Chicago, IL")
                .openingDue(BigDecimal.ZERO)
                .status(Enums.Status.ACTIVE)
                .build());

        // 6. Seed Customers
        Customer walkIn = customerRepository.save(Customer.builder()
                .name("Walk-in Customer")
                .phone("N/A")
                .email("walkin@chocolateshop.local")
                .address("Retail Counter")
                .openingDue(BigDecimal.ZERO)
                .isWalkIn(true)
                .status(Enums.Status.ACTIVE)
                .build());

        Customer regCustomer1 = customerRepository.save(Customer.builder()
                .name("Jonathan Doe")
                .phone("+1-555-4321")
                .email("jonathan.doe@example.com")
                .address("123 Maple Street, New York, NY")
                .openingDue(BigDecimal.ZERO)
                .isWalkIn(false)
                .status(Enums.Status.ACTIVE)
                .build());

        Customer regCustomer2 = customerRepository.save(Customer.builder()
                .name("Sarah Jenkins")
                .phone("+1-555-8765")
                .email("sarah.j@example.com")
                .address("456 Oak Avenue, Brooklyn, NY")
                .openingDue(BigDecimal.ZERO)
                .isWalkIn(false)
                .status(Enums.Status.ACTIVE)
                .build());

        // 7. Seed Initial Purchases to Stock Inventory
        Product pSilk = productRepository.findBySkuIgnoreCase("CHK-CAD-001").orElseThrow();
        Product pKitKat = productRepository.findBySkuIgnoreCase("CHK-NES-002").orElseThrow();
        Product pSnickers = productRepository.findBySkuIgnoreCase("CHK-SNK-003").orElseThrow();
        Product pFerrero = productRepository.findBySkuIgnoreCase("CHK-FER-004").orElseThrow();
        Product pLindtDark = productRepository.findBySkuIgnoreCase("CHK-LND-005").orElseThrow();
        Product pLindtWhite = productRepository.findBySkuIgnoreCase("CHK-LND-006").orElseThrow();
        Product pTobleroneM = productRepository.findBySkuIgnoreCase("CHK-TOB-007").orElseThrow();
        Product pTobleroneD = productRepository.findBySkuIgnoreCase("CHK-TOB-008").orElseThrow();
        Product pTruffles = productRepository.findBySkuIgnoreCase("CHK-MSN-009").orElseThrow();
        Product pPralines = productRepository.findBySkuIgnoreCase("CHK-MSN-010").orElseThrow();
        Product pHazelnut = productRepository.findBySkuIgnoreCase("CHK-MSN-011").orElseThrow();
        Product pRuby = productRepository.findBySkuIgnoreCase("CHK-MSN-012").orElseThrow();
        Product pKinder = productRepository.findBySkuIgnoreCase("CHK-KID-013").orElseThrow();
        Product pGiftBox = productRepository.findBySkuIgnoreCase("CHK-GFT-014").orElseThrow();
        Product pAlmond = productRepository.findBySkuIgnoreCase("CHK-ALM-015").orElseThrow();
        Product pMatcha = productRepository.findBySkuIgnoreCase("CHK-MAT-016").orElseThrow();

        PurchaseRequest purchaseReq1 = PurchaseRequest.builder()
                .supplierId(swissSupplier.getId())
                .paidAmount(new BigDecimal("400.00"))
                .notes("Initial master inventory batch from Zurich")
                .items(Arrays.asList(
                        new PurchaseItemDto(pSilk.getId(), new BigDecimal("50"), pSilk.getPurchasePrice()),
                        new PurchaseItemDto(pKitKat.getId(), new BigDecimal("60"), pKitKat.getPurchasePrice()),
                        new PurchaseItemDto(pFerrero.getId(), new BigDecimal("30"), pFerrero.getPurchasePrice()),
                        new PurchaseItemDto(pLindtDark.getId(), new BigDecimal("40"), pLindtDark.getPurchasePrice()),
                        new PurchaseItemDto(pLindtWhite.getId(), new BigDecimal("35"), pLindtWhite.getPurchasePrice()),
                        new PurchaseItemDto(pTobleroneM.getId(), new BigDecimal("45"), pTobleroneM.getPurchasePrice()),
                        new PurchaseItemDto(pTobleroneD.getId(), new BigDecimal("30"), pTobleroneD.getPurchasePrice())
                ))
                .build();
        purchaseService.createPurchase(purchaseReq1, admin);

        PurchaseRequest purchaseReq2 = PurchaseRequest.builder()
                .supplierId(localSupplier.getId())
                .paidAmount(new BigDecimal("300.00"))
                .notes("Artisan ingredients and handcrafted stock replenishment")
                .items(Arrays.asList(
                        new PurchaseItemDto(pSnickers.getId(), new BigDecimal("40"), pSnickers.getPurchasePrice()),
                        new PurchaseItemDto(pTruffles.getId(), new BigDecimal("25"), pTruffles.getPurchasePrice()),
                        new PurchaseItemDto(pPralines.getId(), new BigDecimal("20"), pPralines.getPurchasePrice()),
                        new PurchaseItemDto(pHazelnut.getId(), new BigDecimal("30"), pHazelnut.getPurchasePrice()),
                        new PurchaseItemDto(pRuby.getId(), new BigDecimal("20"), pRuby.getPurchasePrice()),
                        new PurchaseItemDto(pKinder.getId(), new BigDecimal("35"), pKinder.getPurchasePrice()),
                        new PurchaseItemDto(pGiftBox.getId(), new BigDecimal("15"), pGiftBox.getPurchasePrice()),
                        new PurchaseItemDto(pAlmond.getId(), new BigDecimal("25"), pAlmond.getPurchasePrice()),
                        new PurchaseItemDto(pMatcha.getId(), new BigDecimal("20"), pMatcha.getPurchasePrice())
                ))
                .build();
        purchaseService.createPurchase(purchaseReq2, admin);

        // 8. Seed Initial Completed Sales
        PosSaleRequest saleReq1 = PosSaleRequest.builder()
                .customerId(walkIn.getId())
                .paidAmount(new BigDecimal("25.00"))
                .paymentMethod(Enums.PaymentMethod.CASH)
                .discount(BigDecimal.ZERO)
                .tax(new BigDecimal("1.50"))
                .notes("Counter walk-in customer sale")
                .items(Arrays.asList(
                        new PosCartItem(pSilk.getId(), new BigDecimal("2"), pSilk.getSellingPrice(), BigDecimal.ZERO),
                        new PosCartItem(pKitKat.getId(), new BigDecimal("3"), pKitKat.getSellingPrice(), BigDecimal.ZERO),
                        new PosCartItem(pFerrero.getId(), new BigDecimal("1"), pFerrero.getSellingPrice(), BigDecimal.ZERO)
                ))
                .build();
        saleService.createSale(saleReq1, cashier);

        PosSaleRequest saleReq2 = PosSaleRequest.builder()
                .customerId(regCustomer1.getId())
                .paidAmount(new BigDecimal("35.00"))
                .paymentMethod(Enums.PaymentMethod.CARD)
                .discount(new BigDecimal("2.00"))
                .tax(new BigDecimal("2.20"))
                .notes("VIP member holiday purchase")
                .items(Arrays.asList(
                        new PosCartItem(pTruffles.getId(), new BigDecimal("1"), pTruffles.getSellingPrice(), BigDecimal.ZERO),
                        new PosCartItem(pPralines.getId(), new BigDecimal("1"), pPralines.getSellingPrice(), BigDecimal.ZERO),
                        new PosCartItem(pLindtDark.getId(), new BigDecimal("2"), pLindtDark.getSellingPrice(), BigDecimal.ZERO)
                ))
                .build();
        saleService.createSale(saleReq2, cashier);

        // 9. Seed Expense Categories & Initial Operating Expenses
        ExpenseCategory rentCat = expenseCategoryRepository.save(ExpenseCategory.builder().name("Shop Rent").description("Monthly confectionery boutique rental").build());
        ExpenseCategory utilCat = expenseCategoryRepository.save(ExpenseCategory.builder().name("Electricity & Utilities").description("Shop lighting, cooling and refrigeration").build());
        ExpenseCategory packCat = expenseCategoryRepository.save(ExpenseCategory.builder().name("Packaging & Ribbons").description("Gift boxes, velvet ribbons and thermal wrappers").build());
        ExpenseCategory mktCat = expenseCategoryRepository.save(ExpenseCategory.builder().name("Marketing & Advertising").description("Social media promotions and tasting events").build());

        expenseRepository.save(Expense.builder()
                .category(rentCat)
                .amount(new BigDecimal("450.00"))
                .description("Boutique store floor rent contribution")
                .expenseDate(LocalDate.now())
                .paymentMethod(Enums.PaymentMethod.BANK)
                .recordedBy(manager)
                .build());

        expenseRepository.save(Expense.builder()
                .category(utilCat)
                .amount(new BigDecimal("65.00"))
                .description("Chocolate refrigeration chiller power bill")
                .expenseDate(LocalDate.now())
                .paymentMethod(Enums.PaymentMethod.CASH)
                .recordedBy(manager)
                .build());

        expenseRepository.save(Expense.builder()
                .category(packCat)
                .amount(new BigDecimal("42.50"))
                .description("Gold foil bags and confectionery gift boxes")
                .expenseDate(LocalDate.now())
                .paymentMethod(Enums.PaymentMethod.CASH)
                .recordedBy(cashier)
                .build());

        log.info("Seed data initialization completed successfully!");
    }
}
