package ru.viktorgezz.NauJava.domain.result.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultCommandService;

/**
 * Реализация сервиса управления результатами тестов {@link Result}. Реализует {@link ResultCommandService}.
 */
@Service
public class ResultCommandServiceImpl implements ResultCommandService {

    private final ResultRepo resultRepo;

    @Autowired
    public ResultCommandServiceImpl(ResultRepo resultRepo) {
        this.resultRepo = resultRepo;
    }

    @Override
    @Transactional
    public void deleteResult(Long resultId) {
        if (!resultRepo.existsById(resultId)) {
            throw new EntityNotFoundException("Result with id " + resultId + " not found.");
        }
        resultRepo.deleteById(resultId);
    }
}
