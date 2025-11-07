package ru.viktorgezz.NauJava.domain.report.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report.UserResultReport;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report.UserResultReportRepo;
import ru.viktorgezz.NauJava.domain.report.ReportMapper;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.report.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.UserCountResultReportRepo;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.util.ReportStatusUpdater;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserQueryService;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.currentTimeMillis;

/**
 * Реализация сервиса для управления отчетами. Реализует {@link ReportService}.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final UserQueryService userQueryService;
    private final ResultQueryService resultQueryService;
    private final UserCountResultReportRepo userCountResultReportRepo;
    private final UserResultReportRepo userResultReportRepo;
    private final ReportStatusUpdater reportStatusUpdater;

    @Autowired
    public ReportServiceImpl(
            UserQueryService userQueryService,
            ResultQueryService resultQueryService,
            UserCountResultReportRepo userCountResultReportRepo,
            UserResultReportRepo userResultReportRepo,
            ReportStatusUpdater reportStatusUpdater
    ) {
        this.userQueryService = userQueryService;
        this.resultQueryService = resultQueryService;
        this.userCountResultReportRepo = userCountResultReportRepo;
        this.userResultReportRepo = userResultReportRepo;
        this.reportStatusUpdater = reportStatusUpdater;
    }

    @Override
    public ReportUserCountResultsResponse findById(Long id) {
        ReportUserCountResultsModel model = userCountResultReportRepo
                .findByIdWithFullResults(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND, id));

        List<ResultResponse> resultResponses = model
                .getUserResultReports()
                .stream()
                .map(userResultReport -> {
                    Result result = userResultReport.getResult();
                    return ReportMapper.toDto(
                            result,
                            result.getParticipant().getUsername(),
                            result.getTest().getTitle()
                    );
                })
                .toList();

        return ReportMapper.toDto(model, resultResponses);
    }

    @Override
    @Transactional
    public Long createReport() {
        return userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        ).getId();
    }

    // Ручные Thread - так и надо или я не правильно понял задание?
    @Async
    @Transactional
    @Override
    public CompletableFuture<ReportUserCountResultsResponse> generationReport(Long id) {
        ReportUserCountResultsModel modelFound = userCountResultReportRepo
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND, id));

        final ReportParallelData reportParallel = new ReportParallelData();

        Runnable computeCountUsersRunnable = () -> {
            final long timeBeginStage = currentTimeMillis();
            reportParallel.countUsers.set(userQueryService.computeCountUsers());
            reportParallel.timeSpentSearchingForUsersMillis.set(currentTimeMillis() - timeBeginStage);
        };

        Runnable getResultsRunnable = () -> {
            final long timeBeginStage = currentTimeMillis();
            reportParallel.resultsAtomic.set(resultQueryService.findAllWithParticipantUsernameAndTitleTest());
            reportParallel.timeSpentSearchingForResultsMillis.set(currentTimeMillis() - timeBeginStage);
        };

        try {
            runAndJoinThreadsSafely(computeCountUsersRunnable, getResultsRunnable);
            log.debug("Генерация отчета (id={}) успешно завершена.", id);

            ReportUserCountResultsModel reportUserCountResultsModel =
                    buildAndSaveUserCountResultReportModel(reportParallel, modelFound);
            linkResultAndReport(reportParallel.resultsAtomic.get(), reportUserCountResultsModel);

            List<ResultResponse> resultResponses = reportParallel.resultsAtomic
                    .get()
                    .stream()
                    .map(result ->
                            ReportMapper.toDto(
                                    result,
                                    result.getParticipant().getUsername(),
                                    result.getTest().getTitle()))
                    .toList();

            return CompletableFuture.completedFuture(ReportMapper.toDto(reportUserCountResultsModel, resultResponses));

        } catch (Exception e) {
            log.warn("Генерация отчета (id={}) провалена, сохраняем статус ERROR.", id, e);
            try {
                reportStatusUpdater.saveErrorStatus(id);
            } catch (Exception ex) {
                log.error("Не удалось сохранить статус ERROR для отчета id={}", id, ex);
            }
            return CompletableFuture.failedFuture(new RuntimeException("Ошибка при генерации отчета"));
        }
    }

    @Override
    public List<ReportUserCountResultsResponse> findAllWithFullResults() {
        return userCountResultReportRepo.findAllWithFullResults()
                .stream()
                .map(report -> {
                            List<ResultResponse> resultResponses = report
                                    .getUserResultReports()
                                    .stream()
                                    .map(userResultReport -> {
                                        Result result = userResultReport.getResult();
                                        return ReportMapper.toDto(
                                                result,
                                                result.getParticipant().getUsername(),
                                                result.getTest().getTitle()
                                        );
                                    })
                                    .toList();
                            return ReportMapper.toDto(report, resultResponses);
                        }
                ).toList();
    }

    private void runAndJoinThreadsSafely(Runnable userCountRunnable, Runnable resultsRunnable) {
        final AtomicReference<Throwable> exceptionHolder = new AtomicReference<>(null);
        Thread.UncaughtExceptionHandler handler = (thread, throwable) -> {
            log.error("Критическая ошибка в потоке '{}': {}", thread.getName(), throwable.getMessage(), throwable);
            exceptionHolder.compareAndSet(null, throwable);
        };

        try {
            runAndJoinThreads(userCountRunnable, resultsRunnable, handler);
        } catch (InterruptedException e) {
            log.error("Главный поток генерации отчета был прерван", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Генерация отчета прервана", e);
        }

        if (exceptionHolder.get() != null) {
            throw new RuntimeException(
                    "Ошибка при выполнении фоновой задачи отчета",
                    exceptionHolder.get()
            );
        }
    }

    private void runAndJoinThreads(
            Runnable userCountRunnable,
            Runnable resultsRunnable,
            Thread.UncaughtExceptionHandler exceptionHandler
    ) throws InterruptedException {
        Thread threadUserCount = new Thread(userCountRunnable, "user-count-thread");
        Thread threadResults = new Thread(resultsRunnable, "results-get-thread");
        threadUserCount.setUncaughtExceptionHandler(exceptionHandler);
        threadResults.setUncaughtExceptionHandler(exceptionHandler);

        threadUserCount.start();
        threadResults.start();
        threadUserCount.join();
        threadResults.join();
    }

    private ReportUserCountResultsModel buildAndSaveUserCountResultReportModel(
            ReportParallelData reportParallelData,
            ReportUserCountResultsModel modelFound
    ) {
        modelFound.setStatus(StatusReport.FINISHED);
        modelFound.setCountUsers(reportParallelData.countUsers.get());
        modelFound.setTimeSpentSearchingForUsersMillis(reportParallelData.timeSpentSearchingForUsersMillis.get());
        modelFound.setTimeSpentSearchingForResultsMillis(reportParallelData.timeSpentSearchingForResultsMillis.get());
        modelFound.setTimeSpentCommonMillis(currentTimeMillis() - reportParallelData.startTime);
        modelFound.setCompletedAt(LocalDateTime.now());
        return userCountResultReportRepo.save(modelFound);
    }

    private void linkResultAndReport(List<Result> results, ReportUserCountResultsModel model) {
        List<UserResultReport> joinEntities = results.stream()
                .map(result -> {
                    UserResultReport join = new UserResultReport();
                    join.setUserCountResultReportModel(model);
                    join.setResult(result);
                    return join;
                }).toList();

        userResultReportRepo.saveAll(joinEntities);
    }

    /**
     * Приватный класс-контейнер для хранения атомарных результатов,
     * которые вычисляются в параллельных потоках.
     */
    private static class ReportParallelData {
        final AtomicLong countUsers = new AtomicLong(0);
        final AtomicLong timeSpentSearchingForUsersMillis = new AtomicLong(0);
        final AtomicReference<List<Result>> resultsAtomic = new AtomicReference<>(new ArrayList<>());
        final AtomicLong timeSpentSearchingForResultsMillis = new AtomicLong(0);
        final long startTime = currentTimeMillis();
    }
}
