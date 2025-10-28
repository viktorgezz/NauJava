package ru.viktorgezz.NauJava.result.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.result.service.intrf.ResultQueryService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Реализация сервиса, который читает данные {@link Result}. Реализует {@link ResultQueryService}.
 */
@Service
public class ResultQueryServiceImpl implements ResultQueryService {

    private final ResultRepo resultRepo;

    @Autowired
    public ResultQueryServiceImpl(ResultRepo resultRepo) {
        this.resultRepo = resultRepo;
    }

    @Override
    public List<Result> findAllByGradeAndParticipantId(
            Grade grade,
            Long userId
    ) {
        return resultRepo.findAllByGradeAndParticipantId(grade, userId);
    }

    @Override
    public List<Result> findWithScoreLessThan(BigDecimal maxScore) {
        return resultRepo.findWithScoreLessThan(maxScore);
    }
}
