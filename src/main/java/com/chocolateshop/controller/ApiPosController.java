package com.chocolateshop.controller;

import com.chocolateshop.entity.Product;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.service.ProductService;
import com.chocolateshop.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiPosController {

    private final ProductService productService;
    private final SaleService saleService;

    @GetMapping("/products/search")
    public ResponseEntity<List<Map<String, Object>>> searchProducts(@RequestParam("q") String query) {
        List<Product> products = productService.searchActiveByKeyword(query);
        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("sku", p.getSku());
            map.put("name", p.getName());
            map.put("sellingPrice", p.getSellingPrice());
            map.put("discountPrice", p.getDiscountPrice());
            map.put("effectivePrice", p.getEffectivePrice());
            map.put("stockQuantity", p.getStockQuantity());
            map.put("unit", p.getUnit());
            map.put("category", p.getCategory() != null ? p.getCategory().getName() : "");
            map.put("imagePath", p.getImagePath());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping({"/products", "/pos/products"})
    public ResponseEntity<List<Map<String, Object>>> getAllPosProducts() {
        List<Product> products = productService.getActiveProducts();
        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("sku", p.getSku());
            map.put("name", p.getName());
            map.put("sellingPrice", p.getSellingPrice());
            map.put("discountPrice", p.getDiscountPrice());
            map.put("effectivePrice", p.getEffectivePrice());
            map.put("stockQuantity", p.getStockQuantity());
            map.put("unit", p.getUnit());
            map.put("category", p.getCategory() != null ? p.getCategory().getName() : "");
            map.put("imagePath", p.getImagePath());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        try {
            Product p = productService.getProductById(id);
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("sku", p.getSku());
            map.put("name", p.getName());
            map.put("sellingPrice", p.getSellingPrice());
            map.put("discountPrice", p.getDiscountPrice());
            map.put("effectivePrice", p.getEffectivePrice());
            map.put("stockQuantity", p.getStockQuantity());
            map.put("unit", p.getUnit());
            map.put("category", p.getCategory() != null ? p.getCategory().getName() : "");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/sales/invoice/{invoiceNo}")
    public ResponseEntity<Map<String, Object>> getSaleByInvoice(@PathVariable String invoiceNo) {
        try {
            Sale sale = saleService.getSaleByInvoiceNo(invoiceNo);
            Map<String, Object> map = new HashMap<>();
            map.put("id", sale.getId());
            map.put("invoiceNo", sale.getInvoiceNo());
            map.put("customerName", sale.getCustomer().getName());
            map.put("saleDate", sale.getSaleDate().toString());
            map.put("totalAmount", sale.getTotalAmount());
            map.put("status", sale.getStatus().name());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
