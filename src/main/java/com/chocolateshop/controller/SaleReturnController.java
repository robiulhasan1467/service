package com.chocolateshop.controller;

import com.chocolateshop.dto.SaleReturnRequest;
import com.chocolateshop.entity.AppUser;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.entity.SaleReturn;
import com.chocolateshop.service.SaleService;
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
@RequestMapping("/sales/returns")
@RequiredArgsConstructor
public class SaleReturnController {

    private final SaleService saleService;
    private final UserService userService;

    @GetMapping
    public String listReturns(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        LocalDateTime startDT = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDT = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<SaleReturn> returnPage = saleService.getReturns(
                query, startDT, endDT,
                PageRequest.of(page, size, Sort.by("returnDate").descending())
        );

        model.addAttribute("returnPage", returnPage);
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "sales-return");

        return "sales/returns/list";
    }

    @GetMapping("/new")
    public String newReturnLookup(@RequestParam(value = "invoiceNo", required = false) String invoiceNo, Model model) {
        if (invoiceNo != null && !invoiceNo.isBlank()) {
            try {
                Sale sale = saleService.getSaleByInvoiceNo(invoiceNo.trim());
                model.addAttribute("sale", sale);
                model.addAttribute("returnRequest", new SaleReturnRequest());
            } catch (Exception e) {
                model.addAttribute("errorMessage", "No sale found with invoice: " + invoiceNo);
            }
        }
        model.addAttribute("searchedInvoice", invoiceNo);
        model.addAttribute("activeNav", "sales-return");
        return "sales/returns/new";
    }

    @PostMapping("/process")
    public String processReturn(@Valid @ModelAttribute("returnRequest") SaleReturnRequest request,
                                BindingResult result,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid return request details");
            return "redirect:/sales/returns/new?invoiceNo=" + request.getSaleId();
        }

        try {
            AppUser user = null;
            if (authentication != null) {
                user = userService.getByUsername(authentication.getName());
            }
            SaleReturn saleReturn = saleService.processReturn(request, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sales return " + saleReturn.getReturnNo() + " processed successfully! Stock restored.");
            return "redirect:/sales/returns/view/" + saleReturn.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sales/returns/new";
        }
    }

    @GetMapping("/view/{id}")
    public String viewReturn(@PathVariable Long id, Model model) {
        // Find return by id
        SaleReturn saleReturn = saleService.getReturns(null, null, null, PageRequest.of(0, 500))
                .getContent().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Return not found with id: " + id));

        model.addAttribute("saleReturn", saleReturn);
        model.addAttribute("activeNav", "sales-return");
        return "sales/returns/view";
    }
}
