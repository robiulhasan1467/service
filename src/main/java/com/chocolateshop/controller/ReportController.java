package com.chocolateshop.controller;

import com.chocolateshop.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/profit")
    public String profitReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        ReportService.ProfitSummary summary = reportService.getProfitSummary(startDate, endDate);

        model.addAttribute("summary", summary);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activeNav", "reports-profit");

        return "reports/profit";
    }

    @GetMapping("/inventory")
    public String inventoryReport(Model model) {
        ReportService.InventoryValuation valuation = reportService.getInventoryValuationReport();
        model.addAttribute("valuation", valuation);
        model.addAttribute("activeNav", "reports-inventory");
        return "reports/inventory";
    }

    @GetMapping("/inventory/csv")
    public void exportInventoryCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventory-report-" + LocalDate.now() + ".csv\"");
        String csv = reportService.generateInventoryCsv();
        response.getWriter().write(csv);
    }

    @GetMapping("/dues")
    public String duesReport(Model model) {
        model.addAttribute("customerDues", reportService.getCustomerDueReport());
        model.addAttribute("supplierDues", reportService.getSupplierDueReport());
        model.addAttribute("activeNav", "reports-dues");
        return "reports/dues";
    }
}
