package com.chocolateshop.entity;

/**
 * Common enumeration types used across the Chocolate Shop Management System.
 */
public final class Enums {

    private Enums() {}

    public enum Role {
        ADMIN("Full Access & User Management"),
        MANAGER("Catalog, Purchases & Reports"),
        CASHIER("POS & Sales Orders"),
        INVENTORY_MANAGER("Stock Levels & Ledger");

        private final String description;

        Role(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum PaymentMethod {
        CASH,
        BKASH,
        NAGAD,
        CARD,
        BANK,
        MIXED,
        OTHER
    }

    public enum PurchaseStatus {
        DRAFT,
        CONFIRMED,
        CANCELLED
    }

    public enum SaleStatus {
        COMPLETED,
        PARTIALLY_RETURNED,
        RETURNED,
        CANCELLED
    }

    public enum DeliveryStatus {
        PENDING("Pending / নতুন অর্ডার"),
        CONFIRMED("Confirmed / কনফার্মড"),
        PACKAGING("Packaging / প্যাকেজিং চলছে"),
        SHIPPED("Shipped / কুরিয়ারে হস্তান্তর"),
        DELIVERED("Delivered / ডেলিভারি সম্পন্ন"),
        RETURNED("Returned / রিটার্ন"),
        CANCELLED("Cancelled / বাতিল");

        private final String displayName;

        DeliveryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum TransactionType {
        PURCHASE,
        SALE,
        SALE_RETURN,
        PURCHASE_RETURN,
        DAMAGE,
        ADJUSTMENT,
        OPENING_STOCK
    }
}
