package ru.viktorgezz.NauJava.domain.report.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.report.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.UserCountResultReportRepo;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

/**
 * Утилитный класс для обновления статуса отчета.
 * Использует отдельную транзакцию для гарантии сохранения статуса ERROR
 * даже при откате основной транзакции.
 */
@Component
public class ReportStatusUpdater {

    private static final Logger log = LoggerFactory.getLogger(ReportStatusUpdater.class);

    private final UserCountResultReportRepo userCountResultReportRepo;

    @Autowired
    public ReportStatusUpdater(UserCountResultReportRepo userCountResultReportRepo) {
        this.userCountResultReportRepo = userCountResultReportRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveErrorStatus(Long idReport) {
        ReportUserCountResultsModel report = userCountResultReportRepo
                .findById(idReport)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND, idReport));
        report.setStatus(StatusReport.ERROR);
        userCountResultReportRepo.save(report);
        log.debug("Статус ERROR успешно сохранен для отчета id={}", idReport);
    }
}
