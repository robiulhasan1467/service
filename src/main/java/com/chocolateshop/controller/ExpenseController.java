package com.chocolateshop.controller;

import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Expense;
import com.chocolateshop.entity.ExpenseCategory;
import com.chocolateshop.service.ExpenseService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    @GetMapping
    public String listExpenses(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "paymentMethod", required = false) Enums.PaymentMethod paymentMethod,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {

        Page<Expense> expensePage = expenseService.getExpenses(
                categoryId, paymentMethod, startDate, endDate,
                PageRequest.of(page, size, Sort.by("expenseDate").descending())
        );

        BigDecimal sumTotal = BigDecimal.ZERO;
        if (startDate != null && endDate != null) {
            sumTotal = expenseService.sumExpensesBetween(startDate, endDate);
        }

        model.addAttribute("expensePage", expensePage);
        model.addAttribute("categories", expenseService.getActiveCategories());
        model.addAttribute("paymentMethods", Enums.PaymentMethod.values());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sumTotal", sumTotal);
        model.addAttribute("newExpense", new Expense());
        model.addAttribute("activeNav", "expenses");

        return "expenses/list";
    }

    @PostMapping("/save")
    public String saveExpense(@Valid @ModelAttribute("newExpense") Expense expense,
                              BindingResult result,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid expense details");
            return "redirect:/expenses";
        }

        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            expense.setRecordedBy(user);
            expenseService.saveExpense(expense);
            redirectAttributes.addFlashAttribute("successMessage", "Expense recorded successfully: $" + expense.getAmount());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/expenses";
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.deleteExpense(id);
            redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/expenses";
    }

    @GetMapping("/categories")
    public String listExpenseCategories(Model model) {
        model.addAttribute("categories", expenseService.getAllCategories());
        model.addAttribute("newCategory", new ExpenseCategory());
        model.addAttribute("activeNav", "expense-categories");
        return "expenses/categories";
    }

    @PostMapping("/categories/save")
    public String saveExpenseCategory(@Valid @ModelAttribute("newCategory") ExpenseCategory category,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name is required");
            return "redirect:/expenses/categories";
        }
        try {
            expenseService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Expense category saved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/expenses/categories";
    }
}
