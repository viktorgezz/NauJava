package ru.viktorgezz.NauJava.domain.report.service.intrf;

import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;

import java.util.List;

/**
 * Сервис для управления {@link ReportResultData}.
 * Отвечает за дедупликацию "тяжелых" данных отчетов (JSON с результатами)
 * путем использования хэширования.
 */
public interface ReportResultDataService {

    ReportResultData findOrCreate(List<ResultResponse> results);
}
