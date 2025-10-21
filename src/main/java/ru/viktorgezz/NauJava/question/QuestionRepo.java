package ru.viktorgezz.NauJava.question;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionRepo extends CrudRepository<Question, Long> {

    List<Question> findByTestId(Long testId);

}
