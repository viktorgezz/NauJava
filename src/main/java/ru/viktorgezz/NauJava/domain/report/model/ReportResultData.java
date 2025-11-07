package ru.viktorgezz.NauJava.domain.report.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Хранения уникальных результатов ({@link ResultResponse}) отчетов
 */
@Entity
@Table(name = "report_result_data")
public class ReportResultData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<ResultResponse> results;

    @Column(name = "results_hash", unique = true, nullable = false, length = 64)
    private String resultsHash;

    @OneToMany(
            mappedBy = "reportResultData",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ReportUserCountResultsModel> reports = new ArrayList<>();

    public ReportResultData() {
    }

    public ReportResultData(List<ResultResponse> results, String resultsHash) {
        this.results = results;
        this.resultsHash = resultsHash;
    }

    public void addReport(ReportUserCountResultsModel report) {
        this.reports.add(report);
        report.setReportResultData(this);
    }

    public void removeReport(ReportUserCountResultsModel report) {
        this.reports.remove(report);
        report.setReportResultData(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ResultResponse> getResults() {
        return results;
    }

    public void setResults(List<ResultResponse> results) {
        this.results = results;
    }

    public String getResultsHash() {
        return resultsHash;
    }

    public void setResultsHash(String resultsHash) {
        this.resultsHash = resultsHash;
    }

    public List<ReportUserCountResultsModel> getReports() {
        return reports;
    }

    public void setReports(List<ReportUserCountResultsModel> reports) {
        this.reports = reports;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = (o instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = (this instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;
        ReportResultData testTopic = (ReportResultData) o;

        return getId() != null && Objects.equals(getId(), testTopic.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
