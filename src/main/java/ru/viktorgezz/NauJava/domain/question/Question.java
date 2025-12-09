package ru.viktorgezz.NauJava.domain.question;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Модель вопроса теста.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(precision = 4, scale = 2)
    private BigDecimal point = new BigDecimal("1.00");

    @Column(name = "allow_mistakes")
    private boolean allowMistakes = false;

    @Column(name = "correct_text_answers")
    private List<String> correctTextAnswers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_test")
    private TestModel test;

    @OneToMany(
            mappedBy = "question",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AnswerOption> answerOptions = new ArrayList<>();

    @OneToMany(
            mappedBy = "question",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserAnswer> userAnswers = new ArrayList<>();

    public Question() {
    }

    public Question(String text, Type type, BigDecimal point, List<String> correctTextAnswers, TestModel test, List<AnswerOption> answerOptions, List<UserAnswer> userAnswers) {
        this.text = text;
        this.type = type;
        this.point = point;
        this.correctTextAnswers = correctTextAnswers;
        this.test = test;
        this.answerOptions = answerOptions;
        this.userAnswers = userAnswers;
    }

    public Question(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return type;
    }

    public boolean isAllowMistakes() {
        return allowMistakes;
    }

    public void setAllowMistakes(boolean allowMistakes) {
        this.allowMistakes = allowMistakes;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BigDecimal getPoint() {
        return point;
    }

    public void setPoint(BigDecimal point) {
        this.point = point;
    }

    public List<String> getCorrectTextAnswers() {
        return correctTextAnswers;
    }

    public void setCorrectTextAnswers(List<String> correctTextAnswer) {
        this.correctTextAnswers = correctTextAnswer;
    }

    public TestModel getTest() {
        return test;
    }

    public void setTest(TestModel test) {
        this.test = test;
    }

    public List<AnswerOption> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<AnswerOption> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public List<UserAnswer> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(List<UserAnswer> userAnswers) {
        this.userAnswers = userAnswers;
    }
}
