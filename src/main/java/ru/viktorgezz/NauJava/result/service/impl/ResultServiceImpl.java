package ru.viktorgezz.NauJava.result.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.result.service.intrf.ResultService;

@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepo resultRepo;

    @Autowired
    public ResultServiceImpl(
            ResultRepo resultRepo
    ) {
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
