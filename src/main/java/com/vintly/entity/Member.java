package com.vintly.entity;

import com.vintly.member.model.constant.Use;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Entity(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // JPA 생성 시간 자동 적용
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Comment("이메일")
    @Column(nullable = false, length = 50)
    private String email;

    @Comment("비밀번호")
    @Column(nullable = false)
    private String password;

    @Comment("닉네임")
    @Column(nullable = false)
    private String nickname;

    @Comment("인증번호")
    @Column(name = "email_code")
    private String emailCode;

    @Comment("계정 생성 날짜")
    @Column(name = "create_date", nullable = false)
    @CreatedDate
    private LocalDateTime createDate;

    @Comment("계정 삭제 날짜")
    @Column(name = "del_date")
    private LocalDateTime delDate;

    @Comment("[계정 사용 여부] 사용 : Y, 탈퇴 : N, 추방 : X, 대기 : K")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "use_yn")
    private Use useYn;

    @Comment("계정 레벨")
    @Column(name = "role")
    private String role;

    @Builder
    public Member(String email, String password, String nickname, String role){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.emailCode = "" + ThreadLocalRandom.current().nextInt(100000, 1000000); // 메일 코드 6자리 생성
        this.useYn = Use.K; // 대기
        if(role == null || role.isEmpty()) this.role = "ROLE_USER";
    }

    public void enableMember(){
        this.useYn = Use.Y;
    }
}
