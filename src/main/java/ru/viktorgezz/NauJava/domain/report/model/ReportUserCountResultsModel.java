package ru.viktorgezz.NauJava.domain.report.model;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.domain.report.StatusReport;

import java.time.LocalDateTime;

/**
 * Модель отчета, которые собирает данные о результатах {@link ru.viktorgezz.NauJava.domain.result.Result}
 * и о количестве зарегистрированных пользователей
 */
@Entity
@Table(name = "user_count_result_reports")
public class ReportUserCountResultsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private StatusReport status;

    @Column
    private Long countUsers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_report_result_data")
    private ReportResultData reportResultData;

    @Column
    private Long timeSpentSearchingForUsersMillis;

    @Column
    private Long timeSpentSearchingForResultsMillis;

    @Column
    private Long timeSpentCommonMillis;

    @Column
    private LocalDateTime completedAt;

    public ReportUserCountResultsModel() {
    }

    public ReportUserCountResultsModel(StatusReport status) {
        this.status = status;
    }

    public ReportUserCountResultsModel(StatusReport status, Long countUsers, Long timeSpentSearchingForUsersMillis, Long timeSpentSearchingForResultsMillis, Long timeSpentCommonMillis, LocalDateTime completedAt) {
        this.status = status;
        this.countUsers = countUsers;
        this.timeSpentSearchingForUsersMillis = timeSpentSearchingForUsersMillis;
        this.timeSpentSearchingForResultsMillis = timeSpentSearchingForResultsMillis;
        this.timeSpentCommonMillis = timeSpentCommonMillis;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCountUsers() {
        return countUsers;
    }

    public void setCountUsers(Long countUsers) {
        this.countUsers = countUsers;
    }

    public Long getTimeSpentSearchingForUsersMillis() {
        return timeSpentSearchingForUsersMillis;
    }

    public void setTimeSpentSearchingForUsersMillis(Long timeSpentSearchingForUsersMillis) {
        this.timeSpentSearchingForUsersMillis = timeSpentSearchingForUsersMillis;
    }

    public Long getTimeSpentSearchingForResultsMillis() {
        return timeSpentSearchingForResultsMillis;
    }

    public void setTimeSpentSearchingForResultsMillis(Long timeSpentSearchingForResultsMillis) {
        this.timeSpentSearchingForResultsMillis = timeSpentSearchingForResultsMillis;
    }

    public Long getTimeSpentCommonMillis() {
        return timeSpentCommonMillis;
    }

    public void setTimeSpentCommonMillis(Long timeSpentCommonMillis) {
        this.timeSpentCommonMillis = timeSpentCommonMillis;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public StatusReport getStatus() {
        return status;
    }

    public void setStatus(StatusReport status) {
        this.status = status;
    }

    public ReportResultData getReportResultData() {
        return reportResultData;
    }

    public void setReportResultData(ReportResultData reportResultData) {
        this.reportResultData = reportResultData;
    }
}
