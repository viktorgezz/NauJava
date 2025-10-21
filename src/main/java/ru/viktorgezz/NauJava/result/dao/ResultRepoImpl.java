package ru.viktorgezz.NauJava.result.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class ResultRepoImpl implements ResultRepoCustom {

    private final EntityManager em;

    @Autowired
    public ResultRepoImpl(EntityManager entityManager) {
        this.em = entityManager;
    }

    @Override
    public List<Result> findAllByGradeAndUserId(Grade grade, Long userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Result> cq = cb.createQuery(Result.class);
        Root<Result> root = cq.from(Result.class);

        Predicate gradePredicate = cb.equal(root.get("grade"), grade);
        Predicate userIdPredicate = cb.equal(root.get("user").get("id"), userId);
        cq.where(cb.and(gradePredicate, userIdPredicate));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Result> findWithScoreLessThan(BigDecimal maxScore) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Result> cq = cb.createQuery(Result.class);
        Root<Result> root = cq.from(Result.class);

        Predicate scorePredicate = cb.lessThan(root.get("score"), maxScore);
        cq.where(scorePredicate);

        return em.createQuery(cq).getResultList();
    }
}
