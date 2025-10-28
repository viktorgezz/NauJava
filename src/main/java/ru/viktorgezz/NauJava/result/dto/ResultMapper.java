package ru.viktorgezz.NauJava.result.dto;

import ru.viktorgezz.NauJava.result.Result;

/**
 * Маппер для конвертации {@link Result} в DTO.
 */
public class ResultMapper {

    /**
     * Конвертирует {@link Result} в {@link ResultResponseDto}.
     *
     * @param result модель результата
     * @return DTO результата
     */
    public static ResultResponseDto toDto(Result result) {
        if (result == null) {
            return null;
        }

        ParticipantDto participantDto = null;
        if (result.getParticipant() != null) {
            participantDto = new ParticipantDto(
                    result.getParticipant().getId(),
                    result.getParticipant().getUsername()
            );
        }

        TestInfoDto testInfoDto = null;
        if (result.getTest() != null) {
            testInfoDto = new TestInfoDto(
                    result.getTest().getId(),
                    result.getTest().getTitle()
            );
        }

        return new ResultResponseDto(
                result.getId(),
                result.getScore(),
                result.getGrade(),
                result.getTimeSpentSeconds(),
                result.getCompletedAt(),
                participantDto,
                testInfoDto
        );
    }
}

