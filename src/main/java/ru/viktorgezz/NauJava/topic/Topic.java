package ru.viktorgezz.NauJava.topic;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.test_topic.TestTopic;

import java.util.HashSet;
import java.util.Set;

/**
 * Модель тематической категории тестов.
 * Хранит уникальный заголовок темы и связи с тестами через сущность связывания.
 */
@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @OneToMany(
            mappedBy = "topic",
            fetch = FetchType.LAZY
    )
    private Set<TestTopic> testTopics = new HashSet<>();

    public Topic() {
    }

    public Topic(String title) {
        this.title = title;
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

    public Set<TestTopic> getTestTopics() {
        return testTopics;
    }

    public void setTestTopics(Set<TestTopic> testTopics) {
        this.testTopics = testTopics;
    }
}
