package com.chocolateshop.controller;

import com.chocolateshop.entity.Brand;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("newBrand", new Brand());
        model.addAttribute("activeNav", "brands");
        return "brands/list";
    }

    @PostMapping("/save")
    public String saveBrand(@Valid @ModelAttribute("newBrand") Brand brand,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Brand name is required");
            return "redirect:/brands";
        }

        try {
            if (brand.getStatus() == null) {
                brand.setStatus(Enums.Status.ACTIVE);
            }
            brandService.saveBrand(brand);
            redirectAttributes.addFlashAttribute("successMessage", "Brand saved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/brands";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Brand status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/brands";
    }
}
