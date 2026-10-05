package com.chocolateshop.controller;

import com.chocolateshop.entity.Category;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new Category());
        model.addAttribute("activeNav", "categories");
        return "categories/list";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("newCategory") Category category,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category name is required");
            return "redirect:/categories";
        }

        try {
            if (category.getStatus() == null) {
                category.setStatus(Enums.Status.ACTIVE);
            }
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}
