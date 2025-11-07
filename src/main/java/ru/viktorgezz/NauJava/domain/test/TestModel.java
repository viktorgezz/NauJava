package ru.viktorgezz.NauJava.domain.test;

import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.user.User;

import java.util.*;

/**
 * Модель теста (набора вопросов) с метаданными.
 */
@Entity
@Table(name = "tests")
public class TestModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = true, length = 325)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User author;

    @OneToMany(
            mappedBy = "test",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<TestTopic> testTopics = new HashSet<>();


    @OneToMany(
            mappedBy = "test",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Question> questions = new ArrayList<>();

    @OneToMany(
            mappedBy = "test",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Result> results = new ArrayList<>();

    public TestModel() {
    }

    public TestModel(
            String title,
            String description,
            Status status, User author
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.author = author;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = (o instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = (this instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;
        TestModel testModel = (TestModel) o;

        return getId() != null && Objects.equals(getId(), testModel.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<TestTopic> getTestTopics() {
        return testTopics;
    }

    public void setTestTopics(Set<TestTopic> testTopics) {
        this.testTopics = testTopics;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Result> getTestResults() {
        return results;
    }

    public void setTestResults(List<Result> results) {
        this.results = results;
    }
}
