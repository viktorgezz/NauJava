package ru.viktorgezz.NauJava.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("GroupingUtil Unit Tests")
class GroupingUtilTest {

    @Test
    @DisplayName("buildUserAnswersResponseDto: формирует ответы для SINGLE_CHOICE с выбранным вариантом")
    void buildUserAnswersResponseDto_ShouldMarkSelectedOptions_WhenSingleChoice() {
        Question questionSingleChoice = createQuestionSingleChoice("Question", new BigDecimal("1.00"), null);
        AnswerOption answerOptionFirst = createAnswerOption("A", true, questionSingleChoice);
        answerOptionFirst.setId(1L);
        AnswerOption answerOptionSecond = createAnswerOption("B", false, questionSingleChoice);
        answerOptionSecond.setId(2L);

        List<ResultResponseDto.UserAnswerResponseDto> userAnswersDto = GroupingUtil.buildUserAnswersResponseDto(
                List.of(answerOptionFirst, answerOptionSecond),
                Set.of(answerOptionSecond.getId()),
                null,
                questionSingleChoice
        );

        assertThat(userAnswersDto).hasSize(2);
        ResultResponseDto.UserAnswerResponseDto answerDtoSelected = userAnswersDto.stream()
                .filter(ResultResponseDto.UserAnswerResponseDto::isSelected)
                .findFirst()
                .orElseThrow();
        assertThat(answerDtoSelected.idAnswerOption()).isEqualTo(answerOptionSecond.getId());
        assertThat(answerDtoSelected.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("buildUserAnswersResponseDto: формирует ответ для OPEN_TEXT с проверкой регистра")
    void buildUserAnswersResponseDto_ShouldReturnOpenTextAnswer_WhenOpenTextQuestion() {
        Question questionOpenText = createQuestionOpenText("Text Q", new BigDecimal("2.00"), List.of("Правильный"), null);

        List<ResultResponseDto.UserAnswerResponseDto> userAnswersDto = GroupingUtil.buildUserAnswersResponseDto(
                List.of(),
                Set.of(),
                "правильный",
                questionOpenText
        );

        assertThat(userAnswersDto).hasSize(1);
        ResultResponseDto.UserAnswerResponseDto answerDto = userAnswersDto.getFirst();
        assertThat(answerDto.userTextAnswer()).isEqualTo("правильный");
        assertThat(answerDto.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("buildAnswerOptionMapByQuestionId: группирует варианты ответов по вопросу")
    void buildAnswerOptionMapByQuestionId_ShouldGroupByQuestionId_WhenMultipleQuestions() {
        Question questionFirst = new Question("Q1", Type.SINGLE_CHOICE);
        questionFirst.setId(10L);
        Question questionSecond = new Question("Q2", Type.MULTIPLE_CHOICE);
        questionSecond.setId(20L);
        AnswerOption answerOptionFirst = createAnswerOption("A1", true, questionFirst);
        answerOptionFirst.setId(101L);
        AnswerOption answerOptionSecond = createAnswerOption("A2", false, questionSecond);
        answerOptionSecond.setId(201L);

        Map<Long, List<AnswerOption>> grouped = GroupingUtil.buildAnswerOptionMapByQuestionId(
                List.of(answerOptionFirst, answerOptionSecond)
        );

        assertThat(grouped.keySet()).containsExactlyInAnyOrder(questionFirst.getId(), questionSecond.getId());
        assertThat(grouped.get(questionFirst.getId())).extracting(AnswerOption::getId).containsExactly(answerOptionFirst.getId());
        assertThat(grouped.get(questionSecond.getId())).extracting(AnswerOption::getId).containsExactly(answerOptionSecond.getId());
    }
}
