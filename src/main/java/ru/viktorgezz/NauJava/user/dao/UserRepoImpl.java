package ru.viktorgezz.NauJava.user.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepoImpl implements UserRepoCustom {

    private final EntityManager em;

    @Autowired
    public UserRepoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        Predicate usernamePredicate = cb.equal(root.get("username"), username);
        cq.select(root).where(usernamePredicate);

        try {
            User user = em.createQuery(cq).getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAllByRole(Role role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        Predicate rolePredicate = cb.equal(root.get("role"), role);
        cq.select(root).where(rolePredicate);

        return em.createQuery(cq).getResultList();
    }
}
