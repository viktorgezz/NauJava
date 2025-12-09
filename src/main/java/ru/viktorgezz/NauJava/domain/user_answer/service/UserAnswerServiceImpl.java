package ru.viktorgezz.NauJava.domain.user_answer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.repo.UserAnswerRepo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация сервиса для работы с ответами пользователей. Реализует {@link UserAnswerService}.
 */
@Service
public class UserAnswerServiceImpl implements UserAnswerService {

    private final UserAnswerRepo userAnswerRepo;

    @Autowired
    public UserAnswerServiceImpl(UserAnswerRepo userAnswerRepo) {
        this.userAnswerRepo = userAnswerRepo;
    }

    @Override
    public List<UserAnswer> saveAll(Iterable<UserAnswer> userAnswers) {
        return StreamSupport.stream(userAnswerRepo.saveAll(userAnswers).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAnswer> findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
            Iterable<Long> idsQuestion,
            Long idResult
    ) {
        return userAnswerRepo.findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(idsQuestion, idResult);
    }
}
