package ru.viktorgezz.NauJava.domain.report.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;

import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link ReportResultData}.
 */
public interface ReportResultDataRepo extends CrudRepository<ReportResultData, Long> {

    @Query("""
            SELECT reportData FROM ReportResultData reportData
            LEFT JOIN FETCH reportData.reports
            WHERE reportData.resultsHash = :resultsHash
            """)
    Optional<ReportResultData> findByResultsHash(@Param("resultsHash") String resultsHash);
}
