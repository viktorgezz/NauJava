package ru.viktorgezz.NauJava.domain.test.controller;

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
import ru.viktorgezz.NauJava.security.JwtAuthenticationFilter;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.util.GeneratorRandomModel;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

/**
 * Тесты веб-слоя для {@link TestController}.
 */
@WebMvcTest(
        controllers = TestController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestQueryService testQueryService;

    @Test
    @DisplayName("GET /tests/search/title возвращает список тестов по названию")
    void getTestsByTitle_returnsTests() throws Exception {
        var author = GeneratorRandomModel.getRandomUser();
        author.setId(1L);
        author.setUsername("author1");
        TestModel testModel1 = createTest("Java Core Test", "Desc", Status.PUBLIC, author);
        testModel1.setId(1L);
        TestModel testModel2 = createTest("Java Core Test", "Another", Status.UNLISTED, author);
        testModel2.setId(2L);

        given(testQueryService.findAllByTitle("Java Core Test"))
                .willReturn(List.of(testModel1, testModel2));

        mockMvc.perform(get("/tests/search/title")
                        .param("title", "Java Core Test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Core Test"))
                .andExpect(jsonPath("$[1].title").value("Java Core Test"));

        verify(testQueryService).findAllByTitle("Java Core Test");
        verifyNoMoreInteractions(testQueryService);
    }

    @Test
    @DisplayName("GET /tests/search/topics возвращает тесты по темам")
    void getTestsByTopics_returnsTests() throws Exception {
        var author = GeneratorRandomModel.getRandomUser();
        author.setId(5L);
        author.setUsername("author2");
        TestModel testModel1 = createTest("Java & Spring", "Combined", Status.PUBLIC, author);
        testModel1.setId(10L);
        TestModel testModel2 = createTest("SQL Basics", "SQL", Status.PUBLIC, author);
        testModel2.setId(11L);

        given(testQueryService.findTestsByTopicsTitle(anyList()))
                .willReturn(List.of(testModel1, testModel2));

        mockMvc.perform(get("/tests/search/topics")
                        .param("nameTopics", "Java Basics", "SQL")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.title=='Java & Spring')]").exists())
                .andExpect(jsonPath("$[?(@.title=='SQL Basics')]").exists());

        verify(testQueryService).findTestsByTopicsTitle(List.of("Java Basics", "SQL"));
        verifyNoMoreInteractions(testQueryService);
    }
}