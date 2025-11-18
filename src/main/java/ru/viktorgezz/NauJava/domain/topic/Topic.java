package ru.viktorgezz.NauJava.domain.topic;

import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;

import java.util.HashSet;
import java.util.Objects;
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Class<?> oEffectiveClass = (o instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = (this instanceof HibernateProxy hibernateProxy) ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();

        if (thisEffectiveClass != oEffectiveClass) return false;
        Topic topic = (Topic) o;

        return getId() != null && Objects.equals(getId(), topic.getId());
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

    public Set<TestTopic> getTestTopics() {
        return testTopics;
    }

    public void setTestTopics(Set<TestTopic> testTopics) {
        this.testTopics = testTopics;
    }
}
