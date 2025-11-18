package ru.viktorgezz.NauJava.domain.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация сервиса чтения тестов. Реализует {@link TestQueryService}.
 */
@Service
public class TestQueryServiceImpl implements TestQueryService {

    private final TestRepo testRepo;

    @Autowired
    public TestQueryServiceImpl(TestRepo testRepo) {
        this.testRepo = testRepo;
    }

    @Override
    public List<TestModel> findAll() {
        return StreamSupport
                .stream(testRepo
                        .findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestModel> findAllByTitle(String title) {
        return testRepo.findAllByTitle(title);
    }

    @Override
    public List<TestModel> findTestsByTopicsTitle(List<String> topicTitles) {
        return testRepo.findTestsByTopicTitles(topicTitles);
    }

    @Override
    public List<TestModel> findAllWithAuthorAndTopics() {
        return testRepo.findAllWithAuthorAndTopics();
    }
}
