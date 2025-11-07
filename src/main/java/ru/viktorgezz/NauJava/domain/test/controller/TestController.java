package ru.viktorgezz.NauJava.domain.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.TestMapper;
import ru.viktorgezz.NauJava.domain.test.dto.TestResponseDto;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для тестов {@link TestModel}.
 */
@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestQueryService testQueryService;

    @Autowired
    public TestController(TestQueryService testQueryService) {
        this.testQueryService = testQueryService;
    }

    @GetMapping("/search/title")
    public List<TestResponseDto> getTestsByTitle(@RequestParam String title) {
        return testQueryService.findByTitle(title)
                .stream()
                .map(TestMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search/topics")
    public List<TestResponseDto> getTestsByTopics(@RequestParam List<String> nameTopics) {
        return testQueryService.findTestsByTopicTitles(nameTopics)
                .stream()
                .map(TestMapper::toDto)
                .collect(Collectors.toList());
    }
}
