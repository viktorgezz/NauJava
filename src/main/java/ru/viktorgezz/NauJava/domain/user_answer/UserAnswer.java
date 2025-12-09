package ru.viktorgezz.NauJava.domain.user_answer;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Result;

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

    @Column
    private String textAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

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

    public UserAnswer(Boolean isCorrect, Result result, Question question, AnswerOption answerOption) {
        this.isCorrect = isCorrect;
        this.result = result;
        this.question = question;
        this.answerOption = answerOption;
    }

    public UserAnswer(String textAnswer, Boolean isCorrect, Result result, Question question) {
        this.textAnswer = textAnswer;
        this.isCorrect = isCorrect;
        this.result = result;
        this.question = question;
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

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
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
