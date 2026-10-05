package com.chocolateshop.controller;

import com.chocolateshop.entity.Enums;
import com.chocolateshop.entity.Sale;
import com.chocolateshop.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public String listSales(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) Enums.SaleStatus status,
            @RequestParam(value = "deliveryStatus", required = false) Enums.DeliveryStatus deliveryStatus,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {

        LocalDateTime startDT = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDT = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<Sale> salePage = saleService.getSalesWithDelivery(
                query, status, deliveryStatus, startDT, endDT,
                PageRequest.of(page, size, Sort.by("saleDate").descending())
        );

        model.addAttribute("salePage", salePage);
        model.addAttribute("query", query);
        model.addAttribute("status", status);
        model.addAttribute("deliveryStatus", deliveryStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("statuses", Enums.SaleStatus.values());
        model.addAttribute("deliveryStatuses", Enums.DeliveryStatus.values());
        model.addAttribute("activeNav", "sales-history");

        return "sales/list";
    }

    @PostMapping("/{id}/delivery-status")
    public String updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam("deliveryStatus") Enums.DeliveryStatus deliveryStatus,
            @RequestParam(value = "courierName", required = false) String courierName,
            @RequestParam(value = "trackingCode", required = false) String trackingCode,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Sale sale = saleService.updateDeliveryStatus(id, deliveryStatus, courierName, trackingCode);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + sale.getInvoiceNo() + " delivery status updated to: " + deliveryStatus.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/sales";
    }

    @GetMapping("/invoice/{invoiceNo}")
    public String viewInvoice(@PathVariable String invoiceNo, Model model) {
        Sale sale = saleService.getSaleByInvoiceNo(invoiceNo);
        model.addAttribute("sale", sale);
        model.addAttribute("activeNav", "sales-history");
        return "sales/invoice";
    }
}
