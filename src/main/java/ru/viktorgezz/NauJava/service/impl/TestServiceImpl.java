package ru.viktorgezz.NauJava.service.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.config.PrinterBeginConfig;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.Error;
import ru.viktorgezz.NauJava.model.TestModel;
import ru.viktorgezz.NauJava.repo.TestRepo;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    private final TestRepo testRepo;
    private final PrinterBeginConfig printerBeginConfig;

    @Autowired
    public TestServiceImpl(
            TestRepo testRepo,
            PrinterBeginConfig printerBeginConfig
    ) {
        this.testRepo = testRepo;
        this.printerBeginConfig = printerBeginConfig;
    }

    @PostConstruct
    public void printAppInfoOnStartup() {
        printerBeginConfig.printAppInfoOnStartup();
    }

    @Override
    public void createTest(TestModel test) {
        testRepo.save(test);
    }

    @Override
    public TestModel findById(Long id) {
        return testRepo.findById(id).orElseThrow(() -> new BusinessException(Error.TEST_NOT_FOUND, id));
    }

    @Override
    public List<TestModel> findAll() {
        return testRepo.findAll();
    }

    @Override
    public void deleteById(Long id) {
        testRepo.deleteById(id);
    }

    @Override
    public void updateTitleAndDescription(
            Long id,
            String title,
            String description
    ) {
        TestModel testTemp = findById(id);
        testTemp.setTitle(title);
        testTemp.setDescription(description);

        testRepo.update(testTemp);
    }
}
