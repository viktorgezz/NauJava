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
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataResponseDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;

/**
 * REST-контроллер для тестов {@link TestModel}.
 */
@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestQueryService testQueryService;
    private final TestCommandService testCommandService;
    private final PagedResourcesAssembler<TestMetadataResponseDto> pagedResourcesAssembler;

    @Autowired
    public TestController(
            TestQueryService testQueryService,
            TestCommandService testCommandService,
            PagedResourcesAssembler<TestMetadataResponseDto> pagedResourcesAssembler
    ) {
        this.testQueryService = testQueryService;
        this.testCommandService = testCommandService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @PutMapping("/content")
    public void updateTestContent(@RequestBody @Valid TestUpdateTestContentDto testDto) {
        testCommandService.updateTestContent(testDto);
    }

    @GetMapping("content")
    public TestUpdateTestContentDto getTestContent(@RequestParam("id") Long id) {
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

    @GetMapping
    public PagedModel<EntityModel<TestMetadataResponseDto>> getTests(
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<TestMetadataResponseDto> page = testQueryService.findAllWithAuthorAndTopics(pageable)
                .map(TestMapper::toDto);
        return pagedResourcesAssembler.toModel(page);
    }

    @GetMapping("/title")
    public PagedModel<EntityModel<TestMetadataResponseDto>> getTestsByTitle(
            @RequestParam String title,
            @PageableDefault(
                    size = 20,
                    sort = "title",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        Page<TestMetadataResponseDto> page = testQueryService.findByTitle(title, pageable)
                .map(TestMapper::toDto);
        return pagedResourcesAssembler.toModel(page);
    }
}
