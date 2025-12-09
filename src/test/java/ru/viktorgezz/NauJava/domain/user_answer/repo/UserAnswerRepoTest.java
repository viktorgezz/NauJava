package ru.viktorgezz.NauJava.domain.user_answer.repo;

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
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("UserAnswerRepo Integration Tests")
class UserAnswerRepoTest extends AbstractIntegrationPostgresTest {

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

    @BeforeEach
    void setUp() {
        User userParticipant = userRepo.save(createUserRandom());
        User userAuthor = userRepo.save(createUserRandom());

        TestModel testModel = testRepo.save(createTest("Test", "Desc", Status.PUBLIC, userAuthor));
        questionSaved = questionRepo.save(createQuestionSingleChoice("Question?", new BigDecimal("5.00"), testModel));
        AnswerOption answerOptionCorrect = answerOptionRepo.save(createAnswerOption("Answer A", true, questionSaved));
        AnswerOption answerOptionWrong = answerOptionRepo.save(createAnswerOption("Answer B", false, questionSaved));

        resultSaved = resultRepo.save(createResultWithTest(userParticipant, testModel, null, null, 60));

        userAnswerRepo.save(createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionCorrect, true));
        userAnswerRepo.save(createUserAnswerForChoiceQuestion(resultSaved, questionSaved, answerOptionWrong, false));
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
    @DisplayName("findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption: возврат ответов с подгруженными вопросом и вариантом ответа")
    void findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption_ShouldReturnUserAnswersWithRelations_WhenMatchesExist() {
        List<UserAnswer> userAnswersFound = userAnswerRepo.findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
                List.of(questionSaved.getId()),
                resultSaved.getId()
        );

        assertThat(userAnswersFound).hasSize(2);
        userAnswersFound.forEach(userAnswer -> {
            assertThat(userAnswer.getQuestion()).isNotNull();
            assertThat(userAnswer.getAnswerOption()).isNotNull();
            assertThat(userAnswer.getResult().getId()).isEqualTo(resultSaved.getId());
            assertThat(userAnswer.getQuestion().getId()).isEqualTo(questionSaved.getId());
        });
    }

    @Test
    @DisplayName("findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption: возврат пустого списка при отсутствии совпадений")
    void findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption_ShouldReturnEmptyList_WhenNoMatches() {
        List<UserAnswer> userAnswersFound = userAnswerRepo.findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
                List.of(999L),
                888L
        );

        assertThat(userAnswersFound).isEmpty();
    }
}
