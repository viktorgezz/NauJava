package ru.viktorgezz.NauJava.handler;

import java.util.List;

/**
 * DTO ответа об ошибке для REST API.
 */
public class ErrorResponse {

    private String message;
    private String code;
    private List<ValidationError> validationErrors;

    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public ErrorResponse(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public ErrorResponse() {
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", validationErrors=" + validationErrors +
                '}';
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}