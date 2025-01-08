package com.vintly.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity(name = "refresh")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "refresh_token")
    private String refreshToken;

    private Timestamp expiration;

    @Builder
    public RefreshEntity(String memberId, String refreshToken, Timestamp expiration){
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
