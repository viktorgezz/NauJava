package ru.viktorgezz.NauJava.test_topic;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.topic.Topic;

import java.util.Objects;

/**
 * Связующая модель между тестом и темой (многие-ко-многим через промежуточную таблицу).
 * Гарантирует уникальность пары тест–тема
 */
@Entity
@Table(
        name = "tests_topics",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_test", "id_topic"})
        }
)
public class TestTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_test", nullable = false)
    private TestModel test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_topic", nullable = false)
    private Topic topic;

    public TestTopic() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestModel getTest() {
        return test;
    }

    public void setTest(TestModel test) {
        this.test = test;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TestTopic testTopic = (TestTopic) o;
        return Objects.equals(test, testTopic.test) && Objects.equals(topic, testTopic.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(test, topic);
    }
}
