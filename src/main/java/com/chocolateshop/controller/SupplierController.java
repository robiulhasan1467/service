package com.chocolateshop.controller;

import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Supplier;
import com.chocolateshop.service.SupplierService;
import com.chocolateshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final UserService userService;

    @GetMapping
    public String listSuppliers(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("suppliers", supplierService.getAllSuppliers(query));
        model.addAttribute("query", query);
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/list";
    }

    @GetMapping("/new")
    public String newSupplierForm(Model model) {
        Supplier supplier = new Supplier();
        supplier.setStatus(Enums.Status.ACTIVE);
        model.addAttribute("supplier", supplier);
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/form";
    }

    @GetMapping("/edit/{id}")
    public String editSupplierForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        model.addAttribute("activeNav", "suppliers");
        return "suppliers/form";
    }

    @PostMapping("/save")
    public String saveSupplier(@Valid @ModelAttribute("supplier") Supplier supplier,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        try {
            supplierService.saveSupplier(supplier);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier saved successfully: " + supplier.getName());
            return "redirect:/suppliers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "suppliers/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id);
        BigDecimal currentDue = supplierService.calculateCurrentDue(id);

        model.addAttribute("supplier", supplier);
        model.addAttribute("currentDue", currentDue);
        model.addAttribute("purchases", supplierService.getPurchaseHistory(id));
        model.addAttribute("payments", supplierService.getPaymentHistory(id));
        model.addAttribute("paymentMethods", Enums.PaymentMethod.values());
        model.addAttribute("activeNav", "suppliers");

        return "suppliers/view";
    }

    @PostMapping("/{id}/payment")
    public String recordPayment(@PathVariable Long id,
                                @RequestParam("amount") BigDecimal amount,
                                @RequestParam("paymentMethod") Enums.PaymentMethod paymentMethod,
                                @RequestParam(value = "reference", required = false) String reference,
                                @RequestParam(value = "note", required = false) String note,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            supplierService.recordPayment(id, amount, paymentMethod, reference, note, user);
            redirectAttributes.addFlashAttribute("successMessage", "Payment of $" + amount + " recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/suppliers/view/" + id;
    }
}
