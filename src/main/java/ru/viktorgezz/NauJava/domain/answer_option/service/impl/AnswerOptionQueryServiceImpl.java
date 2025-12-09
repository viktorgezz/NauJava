package ru.viktorgezz.NauJava.domain.answer_option.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;

import java.util.List;

/**
 * Реализация {@link AnswerOptionQueryService}:
 * выборка вариантов ответа с подгруженными вопросами.
 */
@Service
public class AnswerOptionQueryServiceImpl implements AnswerOptionQueryService {

    private final AnswerOptionRepo answerOptionRepo;

    @Autowired
    public AnswerOptionQueryServiceImpl(AnswerOptionRepo answerOptionRepo) {
        this.answerOptionRepo = answerOptionRepo;
    }

    @Override
    public List<AnswerOption> findAllAnswerOptionByIdsQuestionWithQuestion(Iterable<Long> idsQuestion) {
        return answerOptionRepo.findAllByIdsQuestionWithQuestion(idsQuestion);
    }
}
