package ru.viktorgezz.NauJava.domain.report.service.impl;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.report.ReportMapper;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.repo.UserCountResultReportRepo;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportResultDataService;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportService;
import ru.viktorgezz.NauJava.domain.report.util.ReportStatusUpdater;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserQueryService;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.System.currentTimeMillis;

/**
 * Реализация сервиса для управления отчетами. Реализует {@link ReportService}.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final UserQueryService userQueryService;
    private final ResultQueryService resultQueryService;
    private final ReportResultDataService reportResultDataService;
    private final UserCountResultReportRepo userCountResultReportRepo;
    private final ReportStatusUpdater reportStatusUpdater;

    @Autowired
    public ReportServiceImpl(
            UserQueryService userQueryService,
            ResultQueryService resultQueryService,
            ReportResultDataService reportResultDataService,
            UserCountResultReportRepo userCountResultReportRepo,
            ReportStatusUpdater reportStatusUpdater
    ) {
        this.userQueryService = userQueryService;
        this.resultQueryService = resultQueryService;
        this.reportResultDataService = reportResultDataService;
        this.userCountResultReportRepo = userCountResultReportRepo;
        this.reportStatusUpdater = reportStatusUpdater;
    }

    @Override
    public ReportUserCountResultsResponse findById(Long id) {
        ReportUserCountResultsModel report = userCountResultReportRepo
                .findByIdWithResults(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND, id));

        return ReportMapper.toDto(report);
    }

    @Override
    @Transactional
    public Long createReport() {
        return userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        ).getId();
    }

    public CompletableFuture<ReportUserCountResultsResponse> generateReportAsync(Long id) {
        final long startTimeMillis = currentTimeMillis();
        ReportUserCountResultsModel reportToUpdate = userCountResultReportRepo
                .findByIdWithResults(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND, id));

        CompletableFuture<CountUserData> countUsersFuture = computeCountUsersAsync();
        CompletableFuture<TestResults> getResultsFuture = getTestResultsAsync();

        CompletableFuture<ReportUserCountResultsModel> reportUserCountResultsFuture =
                countUsersFuture.thenCombine(getResultsFuture,
                        ((countUserData, testResults) ->
                                fillReportData(
                                        countUserData,
                                        testResults,
                                        reportToUpdate
                                )
                        )
                );

        return reportUserCountResultsFuture
                .thenApply(reportUserCountResults ->
                        handleGenerationSuccess(reportUserCountResults, startTimeMillis))
                .exceptionally(ex ->
                        handleGenerationError(id, ex));
    }

    @Override
    public List<ReportUserCountResultsResponse> findAll() {
        return userCountResultReportRepo.findAll()
                .stream()
                .map(ReportMapper::toDto)
                .toList();
    }

    private CompletableFuture<CountUserData> computeCountUsersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            final long timeBeginStage = currentTimeMillis();
            final long countUsers = userQueryService.computeCountUsers();
            final long durationMillis = currentTimeMillis() - timeBeginStage;

            return new CountUserData(
                    countUsers,
                    durationMillis
            );
        });
    }

    private CompletableFuture<TestResults> getTestResultsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            final long timeBeginStage = currentTimeMillis();

            final List<ResultResponse> resultResponses = resultQueryService
                    .findAllWithParticipantUsernameAndTitleTest()
                    .stream()
                    .map(result ->
                            ReportMapper.toDto(
                                    result,
                                    result.getParticipant().getUsername(),
                                    result.getTest().getTitle()
                            )
                    ).toList();

            final long durationMillis = currentTimeMillis() - timeBeginStage;

            return new TestResults(
                    resultResponses,
                    durationMillis
            );
        });
    }

    private ReportUserCountResultsResponse handleGenerationSuccess(
            ReportUserCountResultsModel reportUserCountResults,
            long startTimeMillis
    ) {
        reportUserCountResults.setTimeSpentCommonMillis(currentTimeMillis() - startTimeMillis);
        reportStatusUpdater.saveFinishedStatus(reportUserCountResults);
        return ReportMapper.toDto(reportUserCountResults);
    }

    private ReportUserCountResultsResponse handleGenerationError(Long idReport, Throwable ex) {
        log.error("Ошибка при генерации отчета id={}: {}", idReport, ex.getMessage(), ex);
        reportStatusUpdater.saveErrorStatus(idReport);
        throw (RuntimeException) ex;
    }

    private ReportUserCountResultsModel fillReportData(
            final CountUserData countUserData,
            final TestResults testResults,
            ReportUserCountResultsModel reportFound
    ) {
        ReportResultData reportResultData = reportResultDataService.findOrCreate(testResults.results());
        reportResultData.addReport(reportFound);

        reportFound.setStatus(StatusReport.FINISHED);
        reportFound.setCountUsers(countUserData.countUser());
        reportFound.setTimeSpentSearchingForUsersMillis(countUserData.timeSpentMs());
        reportFound.setReportResultData(reportResultData);
        reportFound.setTimeSpentSearchingForResultsMillis(testResults.timeSpentMs());
        reportFound.setCompletedAt(LocalDateTime.now());

        return reportFound;
    }

    private record CountUserData(Long countUser, Long timeSpentMs) {
    }

    private record TestResults(List<ResultResponse> results, long timeSpentMs) {
    }
}
