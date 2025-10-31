package ru.viktorgezz.NauJava.result.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.security.JwtAuthenticationFilter;
import ru.viktorgezz.NauJava.util.GeneratorRandomModel;

import static ru.viktorgezz.NauJava.util.CreationModel.*;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты веб-слоя для {@link ResultRestController}.
 */
@WebMvcTest(
        controllers = ResultRestController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class ResultRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResultQueryService resultQueryService;

    @Test
    @DisplayName("GET /results/search/grade-and-participant возвращает результаты по оценке и участнику")
    void getResultsByGradeAndParticipant_returnsResults() throws Exception {
        var user = GeneratorRandomModel.getRandomUser();
        user.setId(42L);
        user.setUsername("participant1");

        Result result1 = createResult(user, Grade.A, new BigDecimal("95.50"));
        result1.setId(1L);

        Result result2 = createResult(user, Grade.A, new BigDecimal("88.00"));
        result2.setId(2L);

        given(resultQueryService.findAllByGradeAndParticipantId(Grade.A, 42L))
                .willReturn(List.of(result1, result2));

        mockMvc.perform(get("/results/search/grade-and-participant")
                        .param("grade", "A")
                        .param("participantId", "42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grade").value("A"))
                .andExpect(jsonPath("$[1].grade").value("A"))
                .andExpect(jsonPath("$[0].participant.id").value(42))
                .andExpect(jsonPath("$[1].participant.id").value(42));

        verify(resultQueryService).findAllByGradeAndParticipantId(Grade.A, 42L);
        verifyNoMoreInteractions(resultQueryService);
    }

    @Test
    @DisplayName("GET /results/search/score-less-than возвращает результаты с баллами ниже порога")
    void getResultsLessScore_returnsResults() throws Exception {
        var user = GeneratorRandomModel.getRandomUser();
        user.setId(7L);
        user.setUsername("participant2");

        Result result1 = createResult(user, Grade.B, new BigDecimal("49.99"));
        result1.setId(10L);

        Result result2 = createResult(user, Grade.C, new BigDecimal("0.00"));
        result2.setId(11L);

        given(resultQueryService.findWithScoreLessThan(new BigDecimal("50")))
                .willReturn(List.of(result1, result2));

        mockMvc.perform(get("/results/search/score-less-than")
                        .param("maxScore", "50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(49.99))
                .andExpect(jsonPath("$[1].score").value(0.00));

        verify(resultQueryService).findWithScoreLessThan(eq(new BigDecimal("50")));
        verifyNoMoreInteractions(resultQueryService);
    }
}