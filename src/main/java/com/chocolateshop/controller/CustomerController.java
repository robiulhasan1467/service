package com.chocolateshop.controller;

import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Customer;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    @GetMapping
    public String listCustomers(@RequestParam(value = "query", required = false) String query, Model model) {
        model.addAttribute("customers", customerService.getAllCustomers(query));
        model.addAttribute("query", query);
        model.addAttribute("activeNav", "customers");
        return "customers/list";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        Customer customer = new Customer();
        customer.setStatus(Enums.Status.ACTIVE);
        model.addAttribute("customer", customer);
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @GetMapping("/edit/{id}")
    public String editCustomerForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("activeNav", "customers");
        return "customers/form";
    }

    @PostMapping("/save")
    public String saveCustomer(@Valid @ModelAttribute("customer") Customer customer,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customers/form";
        }
        try {
            customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer saved successfully: " + customer.getName());
            return "redirect:/customers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "customers/form";
        }
    }

    @GetMapping("/view/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        BigDecimal currentDue = customerService.calculateCurrentDue(id);

        model.addAttribute("customer", customer);
        model.addAttribute("currentDue", currentDue);
        model.addAttribute("sales", customerService.getPurchaseHistory(id));
        model.addAttribute("payments", customerService.getPaymentHistory(id));
        model.addAttribute("paymentMethods", Enums.PaymentMethod.values());
        model.addAttribute("activeNav", "customers");

        return "customers/view";
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
            customerService.recordPayment(id, amount, paymentMethod, reference, note, user);
            redirectAttributes.addFlashAttribute("successMessage", "Payment of $" + amount + " received successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers/view/" + id;
    }
}
