package com.vintly.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity(name = "refresh")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refresh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "refresh_token")
    private String refreshToken;

    private Timestamp expiration;

    @Builder
    public Refresh(String memberId, String refreshToken, Timestamp expiration){
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
