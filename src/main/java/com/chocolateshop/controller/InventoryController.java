package com.chocolateshop.controller;

import com.chocolateshop.dto.StockAdjustmentDto;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.InventoryTransaction;
import com.chocolateshop.entity.Product;
import com.chocolateshop.service.InventoryService;
import com.chocolateshop.service.ProductService;
import com.chocolateshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/stock")
    public String stockOverview(
            @RequestParam(value = "lowStockOnly", defaultValue = "false") boolean lowStockOnly,
            Model model) {

        List<Product> products;
        if (lowStockOnly) {
            products = productService.getLowStockProducts();
        } else {
            products = productService.getActiveProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("lowStockOnly", lowStockOnly);
        model.addAttribute("lowStockCount", productService.countLowStockProducts());
        model.addAttribute("expiringProducts", productService.getExpiringProducts(30));
        model.addAttribute("activeNav", "inventory-stock");

        return "inventory/stock";
    }

    @GetMapping("/history")
    public String inventoryHistory(
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "type", required = false) Enums.TransactionType type,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {

        LocalDateTime startDT = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDT = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<InventoryTransaction> transactionPage = inventoryService.getTransactions(
                productId, type, startDT, endDT,
                PageRequest.of(page, size, Sort.by("transactionDate").descending())
        );

        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("productId", productId);
        model.addAttribute("selectedType", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("types", Enums.TransactionType.values());
        model.addAttribute("activeNav", "inventory-history");

        return "inventory/history";
    }

    @GetMapping("/adjust")
    public String stockAdjustmentForm(Model model) {
        model.addAttribute("adjustmentDto", new StockAdjustmentDto());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("adjustmentTypes", new Enums.TransactionType[]{
                Enums.TransactionType.DAMAGE,
                Enums.TransactionType.ADJUSTMENT,
                Enums.TransactionType.OPENING_STOCK
        });
        model.addAttribute("activeNav", "inventory-adjust");
        return "inventory/adjust";
    }

    @PostMapping("/adjust")
    public String processAdjustment(@Valid @ModelAttribute("adjustmentDto") StockAdjustmentDto dto,
                                    BindingResult result,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("adjustmentTypes", new Enums.TransactionType[]{
                    Enums.TransactionType.DAMAGE,
                    Enums.TransactionType.ADJUSTMENT,
                    Enums.TransactionType.OPENING_STOCK
            });
            model.addAttribute("activeNav", "inventory-adjust");
            return "inventory/adjust";
        }

        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            inventoryService.adjustStock(dto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully");
            return "redirect:/inventory/stock";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("adjustmentTypes", new Enums.TransactionType[]{
                    Enums.TransactionType.DAMAGE,
                    Enums.TransactionType.ADJUSTMENT,
                    Enums.TransactionType.OPENING_STOCK
            });
            model.addAttribute("activeNav", "inventory-adjust");
            return "inventory/adjust";
        }
    }
}
