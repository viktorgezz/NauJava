package ru.viktorgezz.NauJava.domain.user_answer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.repo.UserAnswerRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("UserAnswerService Integration Tests")
class UserAnswerServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserAnswerService userAnswerService;
    @Autowired
    private UserAnswerRepo userAnswerRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private AnswerOptionRepo answerOptionRepo;
    @Autowired
    private ResultRepo resultRepo;

    private Result resultSaved;
    private Question questionSaved;
    private AnswerOption answerOptionCorrect;
    private AnswerOption answerOptionWrong;

    @BeforeEach
    void setUp() {
        User userParticipant = userRepo.save(createUserRandom());
        User userAuthor = userRepo.save(createUserRandom());

        TestModel testModel = testRepo.save(createTest("Test", "Desc", Status.PUBLIC, userAuthor));
        questionSaved = questionRepo.save(createQuestionSingleChoice("Question?", new BigDecimal("5.00"), testModel));
        answerOptionCorrect = answerOptionRepo.save(createAnswerOption("Answer A", true, questionSaved));
        answerOptionWrong = answerOptionRepo.save(createAnswerOption("Answer B", false, questionSaved));

        resultSaved = resultRepo.save(createResultWithTest(userParticipant, testModel, null, null, 60));
    }

    @AfterEach
    void tearDown() {
        userAnswerRepo.deleteAll();
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        resultRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("saveAll: сохраняет ответы и возвращает сохранённые сущности")
    void saveAll_ShouldPersistAndReturnUserAnswers_WhenValid() {
        UserAnswer userAnswerCorrect = createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionCorrect, true);
        UserAnswer userAnswerWrong = createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionWrong, false);

        List<UserAnswer> userAnswersSaved = userAnswerService.saveAll(List.of(userAnswerCorrect, userAnswerWrong));

        assertThat(userAnswersSaved).hasSize(2);
        userAnswersSaved.forEach(userAnswer -> {
            assertThat(userAnswer.getId()).isNotNull();
            assertThat(userAnswer.getResult().getId()).isEqualTo(resultSaved.getId());
        });
        assertThat(userAnswerRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption: возвращает ответы с подгруженными связями")
    void findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption_ShouldReturnUserAnswersWithRelations_WhenMatchesExist() {
        UserAnswer userAnswerCorrect = userAnswerRepo.save(createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionCorrect, true));
        UserAnswer userAnswerWrong = userAnswerRepo.save(createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionWrong, false));

        List<UserAnswer> userAnswersFound = userAnswerService.findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
                List.of(questionSaved.getId()),
                resultSaved.getId()
        );

        assertThat(userAnswersFound).hasSize(2);
        assertThat(userAnswersFound)
                .extracting(UserAnswer::getId)
                .containsExactlyInAnyOrder(userAnswerCorrect.getId(), userAnswerWrong.getId());
        userAnswersFound.forEach(userAnswer -> {
            assertThat(userAnswer.getQuestion()).isNotNull();
            assertThat(userAnswer.getAnswerOption()).isNotNull();
            assertThat(userAnswer.getResult().getId()).isEqualTo(resultSaved.getId());
        });
    }
}
