package com.chocolateshop.repository;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    List<Product> findByStatusOrderByNameAsc(Enums.Status status);

    List<Product> findByCategoryIdAndStatusOrderByNameAsc(Long categoryId, Enums.Status status);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.discountPrice IS NOT NULL AND p.discountPrice < p.sellingPrice ORDER BY ((p.sellingPrice - p.discountPrice) / p.sellingPrice) DESC")
    List<Product> findFlashSaleProducts();

    List<Product> findTop12ByStatusOrderByCreatedAtDesc(Enums.Status status);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchActiveByKeyword(@Param("query") String query, @Param("status") Enums.Status status);

    @Query("SELECT p FROM Product p WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:lowStockOnly = false OR p.stockQuantity <= p.minStockLevel)")
    Page<Product> findWithFilters(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("status") Enums.Status status,
            @Param("lowStockOnly") boolean lowStockOnly,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity <= p.minStockLevel")
    long countLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.stockQuantity <= p.minStockLevel ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.expiryDate IS NOT NULL AND p.expiryDate <= :targetDate ORDER BY p.expiryDate ASC")
    List<Product> findExpiringOrExpiredProducts(@Param("targetDate") LocalDate targetDate);
}
