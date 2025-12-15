package ru.viktorgezz.NauJava.domain.test.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.NauJava.domain.test.TestMapper;

import java.util.List;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataResponseDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.result.dto.ResultShortMetadataResponseDto;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.util.TestJsonConverter;
import ru.viktorgezz.NauJava.security.util.CurrentUserUtils;

/**
 * REST-контроллер для тестов {@link TestModel}.
 */
@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestQueryService testQueryService;
    private final TestCommandService testCommandService;
    private final PagedResourcesAssembler<TestMetadataResponseDto> pagedResourcesAssembler;
    private final ResultQueryService resultQueryService;

    @Autowired
    public TestController(
            TestQueryService testQueryService,
            TestCommandService testCommandService,
            PagedResourcesAssembler<TestMetadataResponseDto> pagedResourcesAssembler,
            ResultQueryService resultQueryService
    ) {
        this.testQueryService = testQueryService;
        this.testCommandService = testCommandService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
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
    public PagedModel<EntityModel<TestMetadataResponseDto>> getTests(
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
        return pagedResourcesAssembler.toModel(page);
    }

    @GetMapping("/title")
    public PagedModel<EntityModel<TestMetadataResponseDto>> getTestsByTitle(
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
        return pagedResourcesAssembler.toModel(page);
    }

    @GetMapping("/{idTest}/results/last")
    public List<ResultShortMetadataResponseDto> getTestLastAttempts(@PathVariable Long idTest) {
        return resultQueryService.findResultLastThreeAttempts(idTest);
    }
}
