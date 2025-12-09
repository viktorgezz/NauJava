package ru.viktorgezz.NauJava.domain.test.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.createTest;
import static ru.viktorgezz.NauJava.util.CreationModel.createUserRandom;

@DisplayName("TestPagingAndSortingRepo Integration Tests")
class TestPagingAndSortingRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestPagingAndSortingRepo testPagingAndSortingRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private UserRepo userRepo;

    private User userAuthor;

    @BeforeEach
    void setUp() {
        userAuthor = userRepo.save(createUserRandom());
    }

    @AfterEach
    void tearDown() {
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("findAllTestIds: возврат первой страницы идентификаторов в порядке возрастания")
    void findAllTestIds_ShouldReturnFirstPageIds_WhenTestsExist() {
        TestModel testFirst = testRepo.save(createTest("Title A", "Desc A", Status.PUBLIC, userAuthor));
        TestModel testSecond = testRepo.save(createTest("Title B", "Desc B", Status.PUBLIC, userAuthor));
        testRepo.save(createTest("Title C", "Desc C", Status.PUBLIC, userAuthor));
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = testPagingAndSortingRepo.findAllTestIds(pageable);

        assertThat(pageIds.getContent()).containsExactly(testFirst.getId(), testSecond.getId());
        assertThat(pageIds.getTotalElements()).isEqualTo(3);
        assertThat(pageIds.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllTestIds: возврат пустой страницы когда тестов нет")
    void findAllTestIds_ShouldReturnEmptyPage_WhenNoTestsExist() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = testPagingAndSortingRepo.findAllTestIds(pageable);

        assertThat(pageIds.getContent()).isEmpty();
        assertThat(pageIds.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findTestIdsByTitle: возврат идентификаторов по подстроке в названии")
    void findTestIdsByTitle_ShouldReturnIdsMatchingTitleSubstring_WhenTestsExist() {
        TestModel testFirst = testRepo.save(createTest("Java Basics", "Desc A", Status.PUBLIC, userAuthor));
        TestModel testSecond = testRepo.save(createTest("Advanced Java", "Desc B", Status.PUBLIC, userAuthor));
        testRepo.save(createTest("Python Intro", "Desc C", Status.PUBLIC, userAuthor));
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        Page<Long> pageIds = testPagingAndSortingRepo.findTestIdsByTitle("java", pageable);

        List<Long> idsFound = pageIds.getContent();
        assertThat(idsFound).containsExactlyInAnyOrder(testFirst.getId(), testSecond.getId());
        assertThat(pageIds.getTotalElements()).isEqualTo(2);
    }
}

