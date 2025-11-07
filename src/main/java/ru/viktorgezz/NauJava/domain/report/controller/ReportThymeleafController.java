package ru.viktorgezz.NauJava.domain.report.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportService;

import java.util.List;

/**
 * Контроллер UI (Thymeleaf) для работы с отчетами {@link ReportUserCountResultsModel}.
 */
@Controller
@RequestMapping("/ui/reports")
public class ReportThymeleafController {

    private static final Logger log = LoggerFactory.getLogger(ReportThymeleafController.class);

    private final ReportService reportService;

    @Autowired
    public ReportThymeleafController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String getReportsPage(
            @RequestParam(name = "id_search", required = false) Long idSearch,
            Model model
    ) {
        try {
            List<ReportUserCountResultsResponse> allReports = reportService.findAll();
            model.addAttribute("allReports", allReports);
        } catch (Exception e) {
            log.error("Не удалось загрузить список отчетов", e);
            model.addAttribute("allReports", List.of());
        }

        if (idSearch != null) {
            try {
                ReportUserCountResultsResponse singleReport = reportService.findById(idSearch);
                model.addAttribute("singleReport", singleReport);
                model.addAttribute("currentSearchId", idSearch);
            } catch (Exception e) {
                model.addAttribute("searchError", "Отчет с ID " + idSearch + " не найден.");
            }
        }

        return "report/reports";
    }

    @PostMapping("/create")
    public String createNewReport() {
        Long idNewReport = reportService.createReport();
        reportService.generateReportAsync(idNewReport);

        return "redirect:/ui/reports";
    }
}
