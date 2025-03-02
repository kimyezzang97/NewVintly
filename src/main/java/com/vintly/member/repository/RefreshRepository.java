package com.vintly.member.repository;

import com.vintly.entity.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

public interface RefreshRepository extends JpaRepository<Refresh, Long> {

    Boolean existsByRefreshToken(String refresh);

    @Transactional
    void deleteByRefreshToken(String refresh);

    boolean deleteByExpirationBefore(Timestamp now);
}
