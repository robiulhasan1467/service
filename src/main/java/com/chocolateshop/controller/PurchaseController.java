package com.chocolateshop.controller;

import com.chocolateshop.dto.PurchaseRequest;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Purchase;
import com.chocolateshop.service.ProductService;
import com.chocolateshop.service.PurchaseService;
import com.chocolateshop.service.SupplierService;
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

@Controller
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public String listPurchases(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) Enums.PurchaseStatus status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        LocalDateTime startDT = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDT = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<Purchase> purchasePage = purchaseService.getPurchases(
                query, status, startDT, endDT,
                PageRequest.of(page, size, Sort.by("purchaseDate").descending())
        );

        model.addAttribute("purchasePage", purchasePage);
        model.addAttribute("query", query);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("statuses", Enums.PurchaseStatus.values());
        model.addAttribute("activeNav", "purchases");

        return "purchases/list";
    }

    @GetMapping("/new")
    public String newPurchaseForm(Model model) {
        model.addAttribute("purchaseRequest", new PurchaseRequest());
        model.addAttribute("suppliers", supplierService.getActiveSuppliers());
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("activeNav", "purchases-new");
        return "purchases/form";
    }

    @PostMapping("/save")
    public String savePurchase(@Valid @ModelAttribute("purchaseRequest") PurchaseRequest request,
                               BindingResult result,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("suppliers", supplierService.getActiveSuppliers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("activeNav", "purchases-new");
            return "purchases/form";
        }

        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            Purchase purchase = purchaseService.createPurchase(request, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Purchase order " + purchase.getInvoiceNo() + " confirmed and stock updated successfully!");
            return "redirect:/purchases/view/" + purchase.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("suppliers", supplierService.getActiveSuppliers());
            model.addAttribute("products", productService.getActiveProducts());
            model.addAttribute("activeNav", "purchases-new");
            return "purchases/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewPurchase(@PathVariable Long id, Model model) {
        Purchase purchase = purchaseService.getPurchaseById(id);
        model.addAttribute("purchase", purchase);
        model.addAttribute("activeNav", "purchases");
        return "purchases/view";
    }

    @PostMapping("/cancel/{id}")
    public String cancelPurchase(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            purchaseService.cancelPurchase(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase order cancelled and stock reversed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/purchases/view/" + id;
    }
}
