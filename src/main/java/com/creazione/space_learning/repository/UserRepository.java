package com.creazione.space_learning.repository;

import com.creazione.space_learning.entities.postgres.UserP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserP, Long> {
    Optional<UserP> findByTelegramId(Long telegramId);
    @Query("SELECT DISTINCT u FROM UserP u LEFT JOIN FETCH u.buildings WHERE u.telegramId = :telegramId")
    Optional<UserP> findByTelegramIdWithBuildings(@Param("telegramId") Long telegramId);
    @Query("SELECT DISTINCT u FROM UserP u LEFT JOIN FETCH u.resources WHERE u.telegramId = :telegramId")
    Optional<UserP> findByTelegramIdWithResources(@Param("telegramId") Long telegramId);
    @Query("SELECT DISTINCT u FROM UserP u " +
            "LEFT JOIN FETCH u.playerScore " +
            "LEFT JOIN FETCH u.buildings " +
            "LEFT JOIN FETCH u.resources " +
            "LEFT JOIN FETCH u.boosters " +
            "LEFT JOIN FETCH u.notices " +
            "WHERE u.telegramId = :telegramId")
    Optional<UserP> findFullUserByTelegramId(@Param("telegramId") Long telegramId);

    // Исправленный count-метод (без FETCH)
    @Query("SELECT COUNT(DISTINCT u.id) FROM UserP u " +
            "JOIN u.buildings b " + // Убрали FETCH!
            "WHERE b.lastUpdate >= :cutoff")
    long countUsersWithRecentBuildingUpdates(@Param("cutoff") Date cutoff);

    // Исправленный метод поиска
    @Query("SELECT DISTINCT u FROM UserP u " +
            "JOIN FETCH u.buildings b " +
            "LEFT JOIN FETCH u.playerScore " + // Убрали псевдоним ps
            "WHERE b.lastUpdate >= :cutoff " +
            "ORDER BY u.id")
    Page<UserP> findUsersWithRecentBuildingUpdates(
            @Param("cutoff") Date cutoff,
            Pageable pageable
    );

    // Исправленный метод поиска
    @Query("SELECT DISTINCT u FROM UserP u " +
            "LEFT JOIN FETCH u.playerScore " +
            "LEFT JOIN FETCH u.buildings " +
            "LEFT JOIN FETCH u.resources " +
            "LEFT JOIN FETCH u.notices " +
            "ORDER BY u.id")
    Page<UserP> findUsersWithResourcesAndRecentBuildingUpdates(
            Pageable pageable
    );

    @Query("SELECT DISTINCT u FROM UserP u " +
            "LEFT JOIN FETCH u.resources " +
            "WHERE u.id = :id")
    Optional<UserP> findUserWithResourcesById(Long id);

    @Query("SELECT u.id FROM UserP u")
    Page<Long> findAllUserIds(Pageable pageable);

    // /**
    // * Обновляет поле name у пользователя по ID
    // *
    // * @param id - ID пользователя
    // * @return Количество обновленных записей
    // */
    @Transactional
    @Modifying
    @Query("UPDATE UserP u SET u.name = :name WHERE u.id = :id")
    int updateNameById(@Param("id") Long id, @Param("name") String name);

    @Query("SELECT u.telegramId FROM UserP u WHERE u.id = :id")
    Optional<Long> findTelegramIdById(Long id);
}