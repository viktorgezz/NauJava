package ru.viktorgezz.testing_system.domain.topic.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import ru.viktorgezz.testing_system.domain.topic.Topic;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link Topic}.
 */
public interface TopicRepo extends CrudRepository<Topic, Long> {

    @NonNull
    List<Topic> findAll();

    Optional<Topic> findByTitle(String title);

    @Query("""
            SELECT DISTINCT topic FROM Topic topic
            JOIN FETCH topic.testTopics testTopic
            JOIN FETCH testTopic.test test
            WHERE test.id = :idTest
            """
    )
    List<Topic> findAllByIdTestModel(@Param("idTest") Long idTestModel);

}
