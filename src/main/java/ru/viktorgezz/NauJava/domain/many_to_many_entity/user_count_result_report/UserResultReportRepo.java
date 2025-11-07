package ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserResultReportRepo extends CrudRepository<UserResultReport, Long> {

    @NonNull
    @Override
    List<UserResultReport> findAll();
}
