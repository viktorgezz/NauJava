package ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.proxy.HibernateProxy;
import ru.viktorgezz.NauJava.domain.report.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.util.Objects;

/**
 * Связующая модель между {@link ReportUserCountResultsModel} и {@link Result} (многие-ко-многим через промежуточную таблицу).
 * Гарантирует уникальность пары result-report
 */
@Entity
@Table(
        name = "user_result_report",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_result", "id_report"})
        }
)
public class UserResultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_result", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Result result;

    @ManyToOne
    @JoinColumn(name = "id_report", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ReportUserCountResultsModel reportUserCountResultsModel;

    public UserResultReport() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public ReportUserCountResultsModel getUserCountResultReportModel() {
        return reportUserCountResultsModel;
    }

    public void setUserCountResultReportModel(ReportUserCountResultsModel reportUserCountResultsModel) {
        this.reportUserCountResultsModel = reportUserCountResultsModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = (o instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = (this instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;
        UserResultReport report = (UserResultReport) o;

        return getId() != null && Objects.equals(getId(), report.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
