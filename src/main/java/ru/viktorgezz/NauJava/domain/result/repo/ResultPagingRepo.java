package ru.viktorgezz.NauJava.domain.result.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.result.Result;

/**
 * Репозиторий для доступа к сущностям {@link Result} с пагинацией и поиском.
 */
@RepositoryRestResource(exported = false)
public interface ResultPagingRepo extends PagingAndSortingRepository<Result, Long> {

    @Query("""
            SELECT result.id FROM Result result
            WHERE result.participant.id = :idUser
            """)
    Page<Long> findAllUserResults(@Param("idUser") Long idUser, Pageable pageable);
}
