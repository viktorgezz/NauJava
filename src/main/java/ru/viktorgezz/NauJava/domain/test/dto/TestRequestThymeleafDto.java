package ru.viktorgezz.NauJava.domain.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Объект передачи данных (DTO) для создания или обновления теста через Thymeleaf-форму.
 */
public class TestRequestThymeleafDto {

    @NotBlank(message = "Название теста не может быть пустым")
    private String title;

    private String description;

    @NotNull(message = "Статус теста не может быть null")
    private String statusParam;

    private String newTopicTitle;

    private String[] selectedTopicIds;

    public TestRequestThymeleafDto() {
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

    public String getStatusParam() {
        return statusParam;
    }

    public void setStatusParam(String statusParam) {
        this.statusParam = statusParam;
    }

    public String getNewTopicTitle() {
        return newTopicTitle;
    }

    public void setNewTopicTitle(String newTopicTitle) {
        this.newTopicTitle = newTopicTitle;
    }

    public String[] getSelectedTopicIds() {
        return selectedTopicIds;
    }

    public void setSelectedTopicIds(String[] selectedTopicIds) {
        this.selectedTopicIds = selectedTopicIds;
    }
}
