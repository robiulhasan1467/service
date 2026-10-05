package com.chocolateshop.repository;

import com.chocolateshop.entity.SaleItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleId(Long saleId);

    @Query("SELECT si.product.id AS productId, si.product.name AS productName, " +
           "SUM(si.quantity) AS totalQty, SUM(si.totalPrice) AS totalSales, SUM(si.profit) AS totalProfit " +
           "FROM SaleItem si WHERE si.sale.status != 'CANCELLED' " +
           "GROUP BY si.product.id, si.product.name ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findBestSellingProducts(Pageable pageable);
}
