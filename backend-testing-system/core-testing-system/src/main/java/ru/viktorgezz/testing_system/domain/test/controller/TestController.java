package ru.viktorgezz.testing_system.domain.test.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.testing_system.domain.result.dto.ResultShortMetadataResponseDto;
import ru.viktorgezz.testing_system.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.testing_system.domain.test.TestMapper;
import ru.viktorgezz.testing_system.domain.test.TestModel;
import ru.viktorgezz.testing_system.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.testing_system.domain.test.dto.TestMetadataResponseDto;
import ru.viktorgezz.testing_system.domain.test.dto.TestToPassDto;
import ru.viktorgezz.testing_system.domain.test.dto.TestUpdateContentDto;
import ru.viktorgezz.testing_system.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.testing_system.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.testing_system.domain.util.TestJsonConverter;
import ru.viktorgezz.testing_system.domain.util.CurrentUserUtils;

import java.util.List;

/**
 * REST-контроллер для тестов {@link TestModel}.
 */
@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestQueryService testQueryService;
    private final TestCommandService testCommandService;
    private final ResultQueryService resultQueryService;

    @Autowired
    public TestController(
            TestQueryService testQueryService,
            TestCommandService testCommandService,
            ResultQueryService resultQueryService
    ) {
        this.testQueryService = testQueryService;
        this.testCommandService = testCommandService;
        this.resultQueryService = resultQueryService;
    }

    @PutMapping("/content")
    public void updateTestContent(@RequestBody @Valid TestUpdateContentDto testDto) {
        testCommandService.updateTestContent(testDto);
    }

    @PutMapping("/content/json")
    public void updateTestContent(@RequestBody String testJson) {
        TestUpdateContentDto testDto = TestJsonConverter.parseDto(testJson);
        testCommandService.updateTestContent(testDto);
    }

    @GetMapping("content")
    public TestUpdateContentDto getTestContent(@RequestParam("id") Long id) {
        return testQueryService.findByIdWithContent(id);
    }

    @GetMapping("/metadata")
    public TestMetadataResponseDto getTestMetadataById(@RequestParam("id") Long id) {
        return TestMapper.toDto(testQueryService.findById(id));
    }

    @PostMapping("/metadata")
    public Long createTest(@RequestBody @Valid TestMetadataRequestDto testDto) {
        return testCommandService.updateTestMetadata(testDto);
    }

    @GetMapping("/{id}")
    public TestToPassDto findTestToPassById(@PathVariable Long id) {
        return testQueryService.findTestToPassById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteTest(@PathVariable Long id) {
        testCommandService.deleteById(id);
    }

    @GetMapping
    public PagedModel<TestMetadataResponseDto> getTests(
            @RequestParam(required = false, defaultValue = "false") Boolean onlyMyTests,
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<TestMetadataResponseDto> page;
        if (Boolean.TRUE.equals(onlyMyTests)) {
            Long userId = CurrentUserUtils.getCurrentUser().getId();
            page = testQueryService.findAllByUserIdWithAuthorAndTopics(userId, pageable)
                    .map(TestMapper::toDto);
        } else {
            page = testQueryService.findAllWithAuthorAndTopics(pageable)
                    .map(TestMapper::toDto);
        }
        return new PagedModel<>(page);
    }

    @GetMapping("/title")
    public PagedModel<TestMetadataResponseDto> getTestsByTitle(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyMyTests,
            @PageableDefault(
                    size = 20,
                    sort = "title",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<TestMetadataResponseDto> page;
        if (Boolean.TRUE.equals(onlyMyTests)) {
            Long userId = CurrentUserUtils.getCurrentUser().getId();
            page = testQueryService.findByUserIdAndTitle(userId, title, pageable)
                    .map(TestMapper::toDto);
        } else {
            page = testQueryService.findByTitle(title, pageable)
                    .map(TestMapper::toDto);
        }
        return new PagedModel<>(page);
    }

    @GetMapping("/{idTest}/results/last")
    public List<ResultShortMetadataResponseDto> getTestLastAttempts(@PathVariable Long idTest) {
        return resultQueryService.findResultLastThreeAttempts(idTest);
    }
}
