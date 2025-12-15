package ru.viktorgezz.NauJava.domain.answer_option.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionCommandService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

import java.util.ArrayList;

/**
 * Реализация {@link AnswerOptionCommandService}:
 * создание, обновление и сохранение связей вариантов ответа с вопросами.
 */
@Service
public class AnswerOptionCommandServiceImpl implements AnswerOptionCommandService {

    private final AnswerOptionRepo answerOptionRepo;

    @Autowired
    public AnswerOptionCommandServiceImpl(AnswerOptionRepo answerOptionRepo) {
        this.answerOptionRepo = answerOptionRepo;
    }

    @Transactional
    @Override
    public AnswerOption createAndLinkAnswerOption(
            Question questionLink,
            TestUpdateContentDto.AnswerOptionDto answerOptionDto
    ) {
        AnswerOption answerOptionNew = new AnswerOption(
                answerOptionDto.text(),
                answerOptionDto.isCorrect(),
                answerOptionDto.explanation(),
                questionLink,
                new ArrayList<>()
        );
        questionLink.getAnswerOptions().add(answerOptionNew);
        return answerOptionNew;
    }

    @Transactional
    @Override
    public void updateAnswerOptionWithoutSave(
            AnswerOption answerOptionExisting,
            TestUpdateContentDto.AnswerOptionDto answerOptionDto
    ) {
        answerOptionExisting.setText(answerOptionDto.text());
        answerOptionExisting.setCorrect(answerOptionDto.isCorrect());
        answerOptionExisting.setExplanation(answerOptionDto.explanation());
    }

    @Transactional
    @Override
    public void saveAll(Iterable<AnswerOption> answerOptions) {
        answerOptionRepo.saveAll(answerOptions);
    }
}
