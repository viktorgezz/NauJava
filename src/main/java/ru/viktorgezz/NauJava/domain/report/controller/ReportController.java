package ru.viktorgezz.NauJava.domain.report.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportService;

import java.util.List;

/**
 * REST-контроллер для report {@link ReportUserCountResultsModel}.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public Long createTestResult() {
        Long idNewReport = reportService.createReport();
        reportService.generateReportAsync(idNewReport);
        return idNewReport;
    }

    @GetMapping("/{id_report}")
    public ReportUserCountResultsResponse getById(@PathVariable(name = "id_report") Long idReport) {
        return reportService.findById(idReport);
    }

    @GetMapping
    public List<ReportUserCountResultsResponse> getAll() {
        return reportService.findAll();
    }
}
