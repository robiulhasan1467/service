package com.chocolateshop.controller;

import com.chocolateshop.dto.QuickOrderRequest;
import com.chocolateshop.entity.Category;
import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Product;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.repository.CategoryRepository;
import com.chocolateshop.repository.ProductRepository;
import com.chocolateshop.repository.SaleRepository;
import com.chocolateshop.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Customer-Facing Storefront Controller for JaHa Live Shopping (https://www.liveshopping.com.bd/ style).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StorefrontController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SaleService saleService;
    private final SaleRepository saleRepository;

    private static final String HOTLINE = "09613-111333";
    private static final String HOTLINE_INTL = "+8809613111333";
    private static final String WHATSAPP = "+8809613111333";

    @ModelAttribute
    public void addCommonAttributes(Model model, Authentication authentication) {
        model.addAttribute("appName", "JaHa");
        model.addAttribute("appTagline", "Live Shopping Bangladesh");
        model.addAttribute("hotline", HOTLINE);
        model.addAttribute("hotlineIntl", HOTLINE_INTL);
        model.addAttribute("whatsappNumber", WHATSAPP);
        model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal()));
        if (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities());
        }
        model.addAttribute("categories", categoryRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE));
    }

    /**
     * Homepage replicating liveshopping.com.bd structure with JaHa branding.
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Product> flashSales = productRepository.findFlashSaleProducts();
        List<Product> allProducts = productRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
        List<Product> newArrivals = productRepository.findTop12ByStatusOrderByCreatedAtDesc(Enums.Status.ACTIVE);

        model.addAttribute("flashSales", flashSales.isEmpty() ? allProducts : flashSales);
        model.addAttribute("allProducts", allProducts);
        model.addAttribute("newArrivals", newArrivals);
        model.addAttribute("quickOrder", new QuickOrderRequest());

        return "home";
    }

    /**
     * Product catalog & filter page.
     */
    @GetMapping("/shop")
    public String shop(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String q,
                       Model model) {
        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findByCategoryIdAndStatusOrderByNameAsc(categoryId, Enums.Status.ACTIVE);
            categoryRepository.findById(categoryId).ifPresent(c -> model.addAttribute("selectedCategory", c));
        } else if (q != null && !q.isBlank()) {
            products = productRepository.searchActiveByKeyword(q.trim(), Enums.Status.ACTIVE);
            model.addAttribute("searchQuery", q);
        } else {
            products = productRepository.findByStatusOrderByNameAsc(Enums.Status.ACTIVE);
        }

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("quickOrder", new QuickOrderRequest());
        return "shop/index";
    }

    /**
     * Single product details page with instant 1-click checkout form.
     */
    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        List<Product> relatedProducts = productRepository.findByCategoryIdAndStatusOrderByNameAsc(
                product.getCategory().getId(), Enums.Status.ACTIVE).stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4)
                .toList();

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        
        QuickOrderRequest orderReq = new QuickOrderRequest();
        orderReq.setProductId(product.getId());
        orderReq.setQuantity(1);
        model.addAttribute("quickOrder", orderReq);

        return "shop/product_details";
    }

    /**
     * Bangladeshi Live Shopping 1-Click Order ("অর্ডার করুন") submission.
     */
    @PostMapping("/quick-order")
    public String processQuickOrder(@Valid @ModelAttribute("quickOrder") QuickOrderRequest request,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            if (request.getProductId() != null) {
                return "redirect:/product/" + request.getProductId();
            }
            return "redirect:/";
        }

        try {
            Sale sale = saleService.createOnlineOrder(request);
            redirectAttributes.addFlashAttribute("successMessage", "আপনার অর্ডারটি সফলভাবে গ্রহণ করা হয়েছে! ইনভয়েস নম্বর: " + sale.getInvoiceNo());
            return "redirect:/order-confirmation/" + sale.getInvoiceNo();
        } catch (Exception e) {
            log.error("Failed to process quick order", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (request.getProductId() != null) {
                return "redirect:/product/" + request.getProductId();
            }
            return "redirect:/";
        }
    }

    /**
     * Order confirmation / receipt page.
     */
    @GetMapping("/order-confirmation/{invoiceNo}")
    public String orderConfirmation(@PathVariable String invoiceNo, Model model) {
        Sale sale = saleRepository.findByInvoiceNo(invoiceNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with invoice: " + invoiceNo));

        model.addAttribute("sale", sale);
        return "shop/order_confirmation";
    }

    /**
     * Order tracking page for Bangladeshi customers.
     */
    @GetMapping("/order-track")
    public String orderTrack(@RequestParam(required = false) String trackingId,
                             @RequestParam(required = false) String phone,
                             Model model) {
        if (trackingId != null && !trackingId.isBlank()) {
            Optional<Sale> saleOpt = saleRepository.findByInvoiceNo(trackingId.trim());
            model.addAttribute("trackedSale", saleOpt.orElse(null));
            model.addAttribute("searchDone", true);
            model.addAttribute("searchQuery", trackingId);
        } else if (phone != null && !phone.isBlank()) {
            List<Sale> sales = saleService.getSalesByCustomerPhone(phone.trim());
            model.addAttribute("phoneSales", sales);
            model.addAttribute("searchDone", true);
            model.addAttribute("searchQuery", phone);
        }
        return "shop/order_track";
    }
}
