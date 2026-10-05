package com.chocolateshop.controller;

import com.chocolateshop.dto.PosSaleRequest;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pos")
@RequiredArgsConstructor
public class PosController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final UserService userService;

    @GetMapping
    public String posScreen(Model model) {
        model.addAttribute("products", productService.getActiveProducts());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("customers", customerService.getActiveCustomers());
        model.addAttribute("walkInCustomer", customerService.getOrCreateWalkInCustomer());
        model.addAttribute("paymentMethods", Enums.PaymentMethod.values());
        model.addAttribute("saleRequest", new PosSaleRequest());
        model.addAttribute("activeNav", "pos");
        return "pos";
    }

    @PostMapping("/checkout")
    public String checkout(@Valid @ModelAttribute("saleRequest") PosSaleRequest request,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            AppUser cashier = null;
            if (authentication != null) {
                cashier = userService.getByUsername(authentication.getName());
            }
            Sale sale = saleService.createSale(request, cashier);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sale completed successfully! Invoice #" + sale.getInvoiceNo());
            return "redirect:/sales/invoice/" + sale.getInvoiceNo();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/pos";
        }
    }
}
