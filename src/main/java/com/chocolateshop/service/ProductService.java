package com.chocolateshop.service;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import com.chocolateshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> getProducts(String query, Long categoryId, Long brandId, Enums.Status status, boolean lowStockOnly, Pageable pageable) {
        return productRepository.findWithFilters(query, categoryId, brandId, status, lowStockOnly, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Product> searchActiveByKeyword(String query) {
        if (query == null || query.isBlank()) {
            return getActiveProducts();
        }
        return productRepository.searchActiveByKeyword(query.trim(), Enums.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
    }

    @Transactional
    public Product saveProduct(Product product) {
        // Validate SKU uniqueness
        if (product.getId() == null) {
            if (productRepository.existsBySkuIgnoreCase(product.getSku())) {
                throw new IllegalArgumentException("Product with SKU '" + product.getSku() + "' already exists");
            }
        } else {
            if (productRepository.existsBySkuIgnoreCaseAndIdNot(product.getSku(), product.getId())) {
                throw new IllegalArgumentException("Another product with SKU '" + product.getSku() + "' already exists");
            }
        }

        // Validate prices
        if (product.getPurchasePrice() == null || product.getPurchasePrice().signum() < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().signum() < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }

        if (product.getStockQuantity() == null) {
            product.setStockQuantity(java.math.BigDecimal.ZERO);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Product product = getProductById(id);
        // Soft delete / deactivation instead of physical deletion
        product.setStatus(product.getStatus() == Enums.Status.ACTIVE ? Enums.Status.INACTIVE : Enums.Status.ACTIVE);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countLowStockProducts() {
        return productRepository.countLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<Product> getExpiringProducts(int withinDays) {
        return productRepository.findExpiringOrExpiredProducts(LocalDate.now().plusDays(withinDays));
    }
}
