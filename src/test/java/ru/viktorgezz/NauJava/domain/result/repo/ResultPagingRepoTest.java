package ru.viktorgezz.NauJava.domain.result.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("ResultPagingRepo Integration Tests")
class ResultPagingRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultPagingRepo resultPagingRepo;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private UserRepo userRepo;

    private User userParticipantFirst;
    private User userParticipantSecond;
    private TestModel testModelFirst;

    @BeforeEach
    void setUp() {
        userParticipantFirst = userRepo.save(createUserRandom());
        userParticipantSecond = userRepo.save(createUserRandom());
        User userAuthorTest = userRepo.save(createUserRandom());

        testModelFirst = createTest("Test Title", "Test Description", Status.PUBLIC, userAuthorTest);
        testRepo.save(testModelFirst);
    }

    @AfterEach
    void tearDown() {
        resultRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("findAllUserResults: возврат первой страницы идентификаторов результатов для пользователя")
    void findAllUserResults_ShouldReturnFirstPageIds_WhenResultsExist() {
        Result resultFirst = resultRepo.save(createResultWithTest(userParticipantFirst, testModelFirst, null, null, 60));
        Result resultSecond = resultRepo.save(createResultWithTest(userParticipantFirst, testModelFirst, null, null, 120));
        resultRepo.save(createResultWithTest(userParticipantFirst, testModelFirst, null, null, 180));
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = resultPagingRepo.findAllUserResults(userParticipantFirst.getId(), pageable);

        assertThat(pageIds.getContent()).containsExactly(resultFirst.getId(), resultSecond.getId());
        assertThat(pageIds.getTotalElements()).isEqualTo(3);
        assertThat(pageIds.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllUserResults: возврат пустой страницы когда результатов нет")
    void findAllUserResults_ShouldReturnEmptyPage_WhenNoResultsExist() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = resultPagingRepo.findAllUserResults(userParticipantFirst.getId(), pageable);

        assertThat(pageIds.getContent()).isEmpty();
        assertThat(pageIds.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findAllUserResults: возврат только результатов указанного пользователя")
    void findAllUserResults_ShouldReturnOnlySpecifiedUserResults_WhenMultipleUsersHaveResults() {
        Result resultFirstUser = resultRepo.save(createResultWithTest(userParticipantFirst, testModelFirst, null, null, 60));
        Result resultSecondUser = resultRepo.save(createResultWithTest(userParticipantSecond, testModelFirst, null, null, 120));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = resultPagingRepo.findAllUserResults(userParticipantFirst.getId(), pageable);

        List<Long> idsFound = pageIds.getContent();
        assertThat(idsFound).containsExactly(resultFirstUser.getId());
        assertThat(idsFound).doesNotContain(resultSecondUser.getId());
        assertThat(pageIds.getTotalElements()).isEqualTo(1);
    }
}

