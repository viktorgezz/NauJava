package ru.viktorgezz.NauJava.model;

import ru.viktorgezz.NauJava.util.IdGenerator;

import java.util.List;

public class TestModel {

    private Long id;

    private String title;

    private String description;

    private List<String> questions;

    public TestModel(
            String title,
            String description,
            List<String> questions
    ) {
        this.id = IdGenerator.getNextId();
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    @Override
    public String toString() {
        return String.format("Test id: %s, title: %s, description: %s, questions: %s",
                id, title, description, questions);
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

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}
