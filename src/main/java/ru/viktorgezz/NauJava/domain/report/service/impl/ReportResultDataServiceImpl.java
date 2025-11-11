package ru.viktorgezz.NauJava.domain.report.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.repo.ReportResultDataRepo;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportResultDataService;

import java.util.List;
import java.util.Optional;


/**
 * Реализует {@link ReportResultDataService}.
 */
@Service
public class ReportResultDataServiceImpl implements ReportResultDataService {

    private final ReportResultDataRepo reportResultDataRepo;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReportResultDataServiceImpl(
            ReportResultDataRepo reportResultDataRepo,
            ObjectMapper objectMapper
    ) {
        this.reportResultDataRepo = reportResultDataRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ReportResultData findOrCreate(List<ResultResponse> results) {
        if (results == null) {
            throw new RuntimeException("results is null");
        }
        try {
            String jsonResults = objectMapper.writeValueAsString(results);
            String hash = DigestUtils.sha256Hex(jsonResults);

            Optional<ReportResultData> reportResultDataFound = reportResultDataRepo.findByResultsHash(hash);
            if (reportResultDataFound.isPresent()) {
                return reportResultDataFound.get();
            }

            ReportResultData reportResultData = new ReportResultData(results, hash);
            return reportResultDataRepo.save(reportResultData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации DTO ResultResponse", e);
        }
    }
}
