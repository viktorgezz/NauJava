package ru.viktorgezz.NauJava.exception;

import org.springframework.http.HttpStatus;

/**
 * Перечень кодов и шаблонов сообщений ошибок бизнес-логики.
 */
public enum ErrorCode {

    USER_NOT_FOUND("User not found", "User with username: %s - not found", HttpStatus.NOT_FOUND),
    REPORT_NOT_FOUND("Report not found", "Report with idTest: %s - not found", HttpStatus.NOT_FOUND),
    TEST_NOT_FOUND("Test not found", "Test with id: %s - not found", HttpStatus.NOT_FOUND),
    RESULT_NOT_FOUND("Result not found", "Result with idResult: %s - not found", HttpStatus.NOT_FOUND),
    RESULT_NOT_READY("Result not ready", "Result with not ready. There are %s questions left", HttpStatus.TOO_EARLY),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Current password and new password are not the same", HttpStatus.BAD_REQUEST),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
    TOKEN_REFRESH_EXPIRED("UNAUTHORIZED", "JWT token is expired",  HttpStatus.UNAUTHORIZED),
    USER_FORBIDDEN("Forbidden", "User with id: %s is not authorized to access this resource", HttpStatus.FORBIDDEN),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(
            String code,
            String defaultMessage,
            HttpStatus status
    ) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
