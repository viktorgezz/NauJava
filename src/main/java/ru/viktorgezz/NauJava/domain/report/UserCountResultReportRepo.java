package ru.viktorgezz.NauJava.domain.report;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link ReportUserCountResultsModel}.
 */
public interface UserCountResultReportRepo extends CrudRepository<ReportUserCountResultsModel, Long> {

    @Query(
            "SELECT r FROM ReportUserCountResultsModel r " +
            "LEFT JOIN FETCH r.userResultReports urr " +
            "LEFT JOIN FETCH urr.result res " +
            "LEFT JOIN FETCH res.participant p " +
            "LEFT JOIN FETCH res.test t " +
            "WHERE r.id = :id"
    )
    Optional<ReportUserCountResultsModel> findByIdWithFullResults(@Param("id") Long id);

    @Query(
            "SELECT DISTINCT r FROM ReportUserCountResultsModel r " +
                    "LEFT JOIN FETCH r.userResultReports urr " +
                    "LEFT JOIN FETCH urr.result res " +
                    "LEFT JOIN FETCH res.participant p " +
                    "LEFT JOIN FETCH res.test t"
    )
    @NonNull
    List<ReportUserCountResultsModel> findAllWithFullResults();

}
