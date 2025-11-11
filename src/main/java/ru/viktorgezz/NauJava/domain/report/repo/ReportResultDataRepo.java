package ru.viktorgezz.NauJava.domain.report.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;

import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link ReportResultData}.
 */
public interface ReportResultDataRepo extends CrudRepository<ReportResultData, Long> {

    Optional<ReportResultData> findByResultsHash(String resultsHash);
}
