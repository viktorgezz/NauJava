package ru.viktorgezz.NauJava.test.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.viktorgezz.NauJava.test.dto.TestRequestThymeleafDto;
import ru.viktorgezz.NauJava.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.topic.TopicService;
import ru.viktorgezz.NauJava.user.User;

/**
 * Контроллер UI (Thymeleaf) для работы с тестами {@link ru.viktorgezz.NauJava.test.TestModel}.
 */
@Controller
@RequestMapping("/ui/tests")
public class TestThymeleafController {

    private final TestQueryService testQueryService;
    private final TestCommandService testCommandService;
    private final TopicService topicService;

    @Autowired
    public TestThymeleafController(
            TestQueryService testQueryService,
            TestCommandService testCommandService,
            TopicService topicService
    ) {
        this.testQueryService = testQueryService;
        this.testCommandService = testCommandService;
        this.topicService = topicService;
    }

    @GetMapping
    public String listTests(Model model) {
        model.addAttribute("tests", testQueryService.findAllWithAuthorAndTopics());
        return "tests/list";
    }

    @GetMapping("/new")
    public String showNewTestForm(Model model) {
        model.addAttribute("topics", topicService.findAll());
        model.addAttribute("testRequestDto", new TestRequestThymeleafDto());
        return "tests/new";
    }

    @PostMapping("/new")
    public String createTest(
            @Valid @ModelAttribute("testRequestDto") TestRequestThymeleafDto dto,
            Model model,
            @AuthenticationPrincipal User author
            ) {
        model.addAttribute("topics", topicService.findAll());
        testCommandService.createTestThymeleaf(dto, author);
        return "redirect:/ui/tests";
    }
}