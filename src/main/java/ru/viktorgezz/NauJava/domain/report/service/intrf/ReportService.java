package ru.viktorgezz.NauJava.domain.report.service.intrf;

import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис получения и управления данными {@link ReportUserCountResultsModel}
 */
public interface ReportService {

    ReportUserCountResultsResponse findById(Long id);

    Long createReport();

    CompletableFuture<ReportUserCountResultsResponse> generateReportAsync(Long id);

    List<ReportUserCountResultsResponse> findAll();
}
