package ru.viktorgezz.NauJava.domain.result.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultCommandService;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;

/**
 * REST-контроллер для чтения результатов прохождения тестов {@link Result}.
 */
@RestController
@RequestMapping("/results")
public class ResultController {

    private final ResultQueryService resultQueryService;
    private final ResultCommandService resultCommandService;

    @Autowired
    public ResultController(
            ResultQueryService resultQueryService,
            ResultCommandService resultCommandService
    ) {
        this.resultQueryService = resultQueryService;
        this.resultCommandService = resultCommandService;
    }

    @PostMapping
    public Long createTestResult(@RequestBody @Valid ResultRequestDto resultRequestDto) {
        Long idResult = resultCommandService.initiateCompilationResult(resultRequestDto);
        resultCommandService.compilateResultAsync(resultRequestDto.idQuestionToUserAnswers(), idResult);
        return idResult;
    }

    @GetMapping("/{id}")
    public ResultResponseDto findTestResult(@PathVariable Long id) {
        return resultQueryService.getTestResultDto(id);
    }
}
