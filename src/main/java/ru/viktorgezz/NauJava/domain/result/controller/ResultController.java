package ru.viktorgezz.NauJava.domain.result.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.ResultMapper;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер для чтения результатов прохождения тестов {@link Result}.
 */
@RestController
@RequestMapping("/results")
public class ResultController {

    private final ResultQueryService resultQueryService;

    @Autowired
    public ResultController(ResultQueryService resultQueryService) {
        this.resultQueryService = resultQueryService;
    }

    @GetMapping("/search/grade-and-participant")
    public List<ResultResponseDto> getResultsByGradeAndParticipant(
            @RequestParam Grade grade,
            @RequestParam Long participantId
            ) {
        return resultQueryService.findAllByGradeAndParticipantId(grade, participantId)
                .stream()
                .map(ResultMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search/score-less-than")
    public List<ResultResponseDto> getResultsLessScore(@RequestParam BigDecimal maxScore) {
        return resultQueryService.findWithScoreLessThan(maxScore)
                .stream()
                .map(ResultMapper::toDto)
                .collect(Collectors.toList());
    }
}