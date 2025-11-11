package ru.viktorgezz.NauJava.domain.report.repo;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link ReportUserCountResultsModel}.
 */
public interface UserCountResultReportRepo extends CrudRepository<ReportUserCountResultsModel, Long> {

    @NonNull
    @Query("SELECT ruc FROM ReportUserCountResultsModel ruc LEFT JOIN FETCH ruc.reportResultData")
    List<ReportUserCountResultsModel> findAll();

    @Query("SELECT ruc FROM ReportUserCountResultsModel ruc " +
            "LEFT JOIN FETCH ruc.reportResultData " +
            "WHERE ruc.id = :id_report"
    )
    Optional<ReportUserCountResultsModel> findByIdWithResults(@Param("id_report") Long id);
}
