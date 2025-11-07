package ru.viktorgezz.NauJava.domain.report.service;

import ru.viktorgezz.NauJava.domain.report.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис получения и управления данными {@link ReportUserCountResultsModel}
 */
public interface ReportService {

    ReportUserCountResultsResponse findById(Long id);

    Long createReport();

    CompletableFuture<ReportUserCountResultsResponse> generationReport(Long id);

    List<ReportUserCountResultsResponse> findAllWithFullResults();

}
