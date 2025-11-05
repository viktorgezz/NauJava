package ru.viktorgezz.NauJava.security;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Date;
import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link RefreshToken}.
 */
@RepositoryRestResource(exported = false)
public interface RefreshTokenRepo extends CrudRepository<RefreshToken, Long> {

    @Modifying
    void deleteByRefreshToken(String refreshToken);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.username = :username")
    List<RefreshToken> findRefreshTokensByUsername(String username);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.dateExpiration < :dateNow")
    void deleteExpiredTokens(@Param("dateNow") Date dateNow);

}
