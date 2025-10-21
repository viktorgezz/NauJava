package ru.viktorgezz.NauJava.test.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test_topic.TestTopic;

import java.util.Collections;
import java.util.List;

@Repository
public class TestRepoImpl implements TestRepoCustom {

    private final EntityManager em;

    @Autowired
    public TestRepoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<TestModel> findByTitle(String title) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestModel> cq = cb.createQuery(TestModel.class);
        Root<TestModel> root = cq.from(TestModel.class);

        Predicate predicate = cb.equal(root.get("title"), title);
        cq.where(predicate);

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<TestModel> findTestsByTopicTitles(List<String> topicTitles) {
        if (topicTitles == null || topicTitles.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TestModel> cq = cb.createQuery(TestModel.class);
        Root<TestTopic> testTopicRoot = cq.from(TestTopic.class);

        Path<TestModel> testPath = testTopicRoot.get("test");
        cq.select(testPath);
        cq.distinct(true);

        Path<String> topicTitlePath = testTopicRoot.get("topic").get("title");

        Predicate topicTitlePredicate = topicTitlePath.in(topicTitles);

        cq.where(topicTitlePredicate);

        return em.createQuery(cq).getResultList();
    }
}
