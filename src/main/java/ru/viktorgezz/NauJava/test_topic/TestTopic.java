package ru.viktorgezz.NauJava.test_topic;

import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;
import ru.viktorgezz.NauJava.result.Result;
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = (o instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = (this instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;
        TestTopic testTopic = (TestTopic) o;

        return getId() != null && Objects.equals(getId(), testTopic.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
