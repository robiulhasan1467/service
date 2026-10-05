package com.chocolateshop.controller;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import com.chocolateshop.service.BrandService;
import com.chocolateshop.service.CategoryService;
import com.chocolateshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "status", required = false) Enums.Status status,
            @RequestParam(value = "lowStock", defaultValue = "false") boolean lowStock,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        Page<Product> productPage = productService.getProducts(
                query, categoryId, brandId, status, lowStock,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("query", query);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("status", status);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("activeNav", "products");

        return "products/list";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        Product product = new Product();
        product.setStatus(Enums.Status.ACTIVE);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("activeNav", "products");
        return "products/form";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("activeNav", "products");
        return "products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            model.addAttribute("activeNav", "products");
            return "products/form";
        }

        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully: " + product.getName());
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("brands", brandService.getActiveBrands());
            model.addAttribute("activeNav", "products");
            return "products/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }
}
