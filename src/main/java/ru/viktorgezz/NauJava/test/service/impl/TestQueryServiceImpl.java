package ru.viktorgezz.NauJava.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.repo.TestRepo;
import ru.viktorgezz.NauJava.test.service.intrf.TestQueryService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация сервиса чтения тестов. Реализует {@link ru.viktorgezz.NauJava.test.service.intrf.TestQueryService}.
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
    public List<TestModel> findByTitle(String title) {
        return testRepo.findByTitle(title);
    }

    @Override
    public List<TestModel> findTestsByTopicTitles(List<String> topicTitles) {
        return testRepo.findTestsByTopicTitles(topicTitles);
    }

    @Override
    public List<TestModel> findAllWithAuthorAndTopics() {
        return testRepo.findAllWithAuthorAndTopics();
    }
}
