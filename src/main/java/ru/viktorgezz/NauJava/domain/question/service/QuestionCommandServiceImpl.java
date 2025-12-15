package ru.viktorgezz.NauJava.domain.question.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Реализация {@link QuestionCommandService} для управления вопросами в тестах.
 */
@Service
public class QuestionCommandServiceImpl implements QuestionCommandService {

    private final QuestionRepo questionRepo;

    @Autowired
    public QuestionCommandServiceImpl(QuestionRepo questionRepo) {
        this.questionRepo = questionRepo;
    }

    @Override
    @Transactional
    public Question createQuestionWithoutSave(TestUpdateContentDto.QuestionDto questionDto, TestModel test) {
        return new Question(
                questionDto.text(),
                questionDto.type(),
                questionDto.point(),
                Optional.ofNullable(questionDto.correctTextAnswer())
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new),
                test,
                new ArrayList<>(),
                new ArrayList<>(),
                questionDto.allowMistakes()
        );
    }

    @Override
    @Transactional
    public void updateQuestionWithoutSave(
            Question question,
            TestUpdateContentDto.QuestionDto questionDto
    ) {
        question.setText(questionDto.text());
        question.setType(questionDto.type());
        question.setPoint(questionDto.point());
        question.setCorrectTextAnswers(Optional.ofNullable(questionDto.correctTextAnswer())
                .map(ArrayList::new)
                .orElseGet(ArrayList::new));
        question.setAllowMistakes(questionDto.allowMistakes());
    }

    @Override
    @Transactional
    public void saveAll(Iterable<Question> questions) {
        questionRepo.saveAll(questions);
    }
}
