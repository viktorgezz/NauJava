package ru.viktorgezz.NauJava.user_answer;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.question.Question;
import ru.viktorgezz.NauJava.result.Result;

/**
 * Модель ответа пользователя на конкретный вопрос.
 * Может содержать текстовый ответ или ссылку на выбранный вариант,
 * а также связь с результатом прохождения теста и самим вопросом.
 */
@Entity
@Table(name = "user_answers")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String textAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_test_result")
    private Result result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_question")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_answer_option")
    private AnswerOption answerOption;

    public UserAnswer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public Result getTestResult() {
        return result;
    }

    public void setTestResult(Result result) {
        this.result = result;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public AnswerOption getAnswerOption() {
        return answerOption;
    }

    public void setAnswerOption(AnswerOption answerOption) {
        this.answerOption = answerOption;
    }
}
