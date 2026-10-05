# 🍫 Chocolate Shop Management System (La Maison du Chocolat)

A complete, production-grade ERP and Point of Sale (POS) Management System tailored for retail and artisan confectionery businesses. Built using **Java 21**, **Spring Boot 4.1.x**, **Spring Data JPA**, **Spring Security**, **PostgreSQL**, **Thymeleaf**, **Bootstrap 5**, and **Chart.js**.

---

## 🌟 Key Modules & Features

### 1. Executive Dashboard (`/dashboard`)
- Real-time KPIs: Today's Sales, Month-to-Date Revenue, Realized Gross Margin, Low-Stock Alerts, Outstanding Customer Dues, and Active Inward Shipments.
- 7-Day interactive Sales and Profit trend visual charts powered by Chart.js.
- Best-selling chocolates ranking and quick recent orders ledger.

### 2. Fast Point of Sale (POS) (`/pos`)
- Live category filtering and instant product search / barcode SKU scanning.
- Interactive cart management with dynamic line totals, item discounts, customizable sales tax, and auto-calculated tender change / due balances.
- Split-payment support (`CASH`, `CARD`, `BKASH`, `NAGAD`, `BANK`).
- Instant thermal receipt and standard invoice printing (`/sales/invoice/{invoiceNo}`).

### 3. Sales History & Invoicing (`/sales`)
- Complete searchable ledger of all customer transactions.
- Filter by date range, invoice number, payment status, and order status.
- Printable 80mm compact POS receipts and full-page tax invoices.

### 4. Sales Return & Refund Management (`/sales/returns`)
- Customer invoice lookup and line item selection.
- Return quantity validation prevents over-returning items.
- Automatic inventory replenishment into stock with ledger tracking.
- Proportional profit deduction from financial reports.

### 5. Product Catalog & Confectionery Categories (`/products`, `/categories`, `/brands`)
- Full chocolate lifecycle: SKU, barcode, unit (box, bar, pack, kg), cost price, retail price, promotional price, min/max stock thresholds, and expiration alerts.
- Product classification under Categories (Dark, Milk, White, Pralines, Truffles, Gift Boxes) and Brands (Lindt, Godiva, Ferrero Rocher, Valrhona, House Artisan).
- Soft-delete safeguards: products with historical sales are archived rather than permanently purged.

### 6. Inventory & Immutable Stock Ledger (`/inventory/stock`, `/inventory/history`, `/inventory/adjust`)
- Real-time stock visibility with color-coded stock warnings (low-stock, out-of-stock).
- Immutable ledger (`inventory_transactions`) recording every movement (`PURCHASE`, `SALE`, `SALE_RETURN`, `DAMAGE`, `ADJUSTMENT`).
- Physical audit recount adjustments and damaged chocolate write-offs (e.g. melting during transit).

### 7. Supplier Procurement & Purchase Orders (`/purchases`, `/suppliers`)
- Multi-item purchase order generation with automatic stock replenishment upon confirmation.
- Order cancellation with automatic inventory reversal and audit logging.
- Supplier profiles, order histories, and accounts payable due tracking.
- Partial and full payment recording against supplier bills.

### 8. Customer Relationship Management (`/customers`)
- Default instant walk-in customer support (`Walk-in Customer`).
- Registered customer profiles with purchase history, credit limits, and outstanding receivable due tracking.
- Due collection receipts and settlement recording.

### 9. Operating Overhead & Expense Tracking (`/expenses`, `/expenses/categories`)
- Logging of day-to-day operating expenditures (Rent, Packaging Boxes, Cold-Chain Logistics, Utilities, Marketing).
- Expense category classification and date-filtered totals.

### 10. Financial Statements & Executive Reports (`/reports/profit`, `/reports/inventory`, `/reports/dues`)
- **Profit & Loss Statement**: Audited statement calculating Gross Revenue, Cost of Goods Sold, Gross Margin, Operating Overhead, and Net Operating Profit.
- **Inventory Valuation Report**: Asset valuation comparing Total Cost Value, Potential Retail Revenue, and Unrealized Profit with one-click **CSV export**.
- **Credit & Due Balances**: Accounts Receivable (Customer dues) and Accounts Payable (Supplier dues) monitoring.

### 11. Role-Based Access Control (`/users`)
- Database-backed authentication with BCrypt password hashing.
- Four distinct enterprise roles:
  - `ADMIN`: Unrestricted system access, user administration, financial auditing.
  - `MANAGER`: Product catalog, inward procurement, customer/supplier ledgers, returns, financial reports.
  - `INVENTORY_MANAGER`: Products, stock recounts, purchase receipts, damage write-offs.
  - `CASHIER`: Point of Sale (POS), customer profile search, sales order history.

---

## 🔑 Default Credentials & Role Matrix

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `Admin@123` | `ROLE_ADMIN` | Store Owner / General Manager |
| `manager` | `Manager@123` | `ROLE_MANAGER` | Retail Operations Manager |
| `cashier` | `Cashier@123` | `ROLE_CASHIER` | Front-Desk POS Cashier |
| `inventory` | `Inventory@123` | `ROLE_INVENTORY_MANAGER` | Stock & Warehouse Custodian |

---

## 💻 Tech Stack & Architecture

- **Backend**: Java 21, Spring Boot 4.1.x (Spring MVC, Spring Data JPA, Spring Security)
- **Database**: PostgreSQL 18+ (HikariCP connection pool)
- **Frontend**: Thymeleaf SSR, Bootstrap 5.3, Bootstrap Icons, Vanilla JS, Chart.js
- **Aesthetic**: Artisan Confectionery Dark & Gold Theme (Warm Cacao, Champagne Gold, Cream Linen)

```
com.chocolateshop
├── config         # Security, WebMvc, and seed data initialization (DataInitializer)
├── controller     # Web MVC & REST endpoints (POS, Products, Purchases, Reports, etc.)
├── service        # Business logic services (SaleService, InventoryService, etc.)
├── repository     # 18 Spring Data JPA repository interfaces
├── entity         # Domain models (Sale, Product, InventoryTransaction, Expense, etc.)
├── dto            # Request payloads & form data transfer objects
├── exception      # Centralized exception handling (@ControllerAdvice)
├── security       # CustomUserDetailsService & SecurityFilterChain
└── util           # Helper utilities
```

---

## ⚙️ PostgreSQL Configuration

Configured via environment variables with defaults in `src/main/resources/application.properties`:

| Variable | Property Key | Default Value | Description |
|----------|--------------|---------------|-------------|
| `DB_HOST` | `spring.datasource.url` | `localhost` | Database host |
| `DB_PORT` | `spring.datasource.url` | `5432` | Database port |
| `DB_NAME` | `spring.datasource.url` | `chocolate_shop` | Database name |
| `DB_USERNAME` | `spring.datasource.username` | `root` | Database username |
| `DB_PASSWORD` | `spring.datasource.password` | `Quanfey` | Database password |
| `SERVER_PORT` | `server.port` | `8080` | Web server port |

---

## 🚀 Running the Application Locally

```bash
# 1. Ensure Java 21 & Maven are configured
export JAVA_HOME=/opt/jdk-21.0.9
export PATH=$JAVA_HOME/bin:/opt/maven-3.9.12/bin:$PATH

# 2. Build and run integration tests
mvn clean package

# 3. Start the application
java -jar target/chocolate-shop-1.0.0.jar
```

The system will start on **`http://localhost:8080`**.
Log in using `admin` / `Admin@123` to access all features.
