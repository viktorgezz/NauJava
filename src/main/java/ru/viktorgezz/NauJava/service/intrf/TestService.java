package ru.viktorgezz.NauJava.service.intrf;

import ru.viktorgezz.NauJava.model.TestModel;

import java.util.List;

public interface TestService {

    void createTest(TestModel test);

    TestModel findById(Long id);

    List<TestModel> findAll();

    void deleteById(Long id);

    void updateTitleAndDescription(Long id, String title, String description);
}
