package ru.viktorgezz.NauJava.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.model.TestModel;
import ru.viktorgezz.NauJava.util.TestArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class TestRepoImpl implements TestRepo {

    private final TestArrayList testList;

    @Autowired
    public TestRepoImpl(TestArrayList testList) {
        this.testList = testList;
    }

    @Override
    public void save(TestModel entity) {
        testList.add(entity);
    }

    @Override
    public Optional<TestModel> findById(Long id) {
        return testList
                .stream()
                .filter(t -> t.getId().compareTo(id) == 0)
                .findFirst();
    }

    @Override
    public List<TestModel> findAll() {
        return new ArrayList<>(testList);
    }

    @Override
    public void update(TestModel entity) {
        IntStream.range(0, testList.size())
                .filter(i -> testList.get(i).getId().equals(entity.getId()))
                .findFirst()
                .ifPresent(i -> testList.set(i, entity));
    }

    @Override
    public void deleteById(Long id) {
        IntStream.range(0, testList.size())
                .filter(i -> testList.get(i).getId().equals(id))
                .findFirst()
                .ifPresent(testList::remove);
    }
}
