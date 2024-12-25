package com.vintly.entity;

import com.vintly.member.constant.Use;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "email_code")
    private String emailCode;

    @Column(name = "email_ex_date")
    private LocalDateTime emailExDate;

    @Column(name = "del_date")
    private LocalDateTime delDate;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "use_yn")
    private Use useYn;

    @Builder
    public Member(String email, String password, String nickname){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.emailCode = "" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        this.emailExDate = LocalDateTime.now().plusDays(3); // 만든 날짜보다 3일 후 입력
        this.useYn = Use.K; // 대기
    }

    public void enableMember(){
        this.useYn = Use.Y;
    }
}
