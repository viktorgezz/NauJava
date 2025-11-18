package ru.viktorgezz.NauJava.domain.topic;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link Topic}.
 */
@RepositoryRestResource(path = "topics")
public interface TopicRepo extends CrudRepository<Topic, Long> {

    @NonNull
    List<Topic> findAll();

    @Query("""
            SELECT DISTINCT topic FROM Topic topic
            JOIN FETCH topic.testTopics testTopic
            JOIN FETCH testTopic.test test
            WHERE test.id = :idTest
            """
    )
    List<Topic> findAllByIdTestModel(@Param("idTest") Long idTestModel);

}
