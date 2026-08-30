package com.vintly.domain.member.entity;

import com.vintly.domain.BaseEntity;
import com.vintly.domain.member.Use;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // JPA 생성 시간 자동 적용
@Entity(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Comment("이메일")
    @Column(nullable = false, length = 64)
    private String email;

    @Comment("비밀번호")
    @Column(nullable = false)
    private String password;

    @Comment("닉네임")
    @Column(nullable = false, length = 30)
    private String nickname;

    @Comment("인증번호")
    @Column(name = "email_code", length = 20)
    private String emailCode;

    @Comment("계정 레벨")
    @Column(name = "role")
    private String role;

    @Comment("[계정 이용 상태] 사용: Y, 추방: X, 대기: K, 탈퇴: E")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "use_status")
    private Use useStatus;

    @Comment("계정 삭제 날짜")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Comment("닉네임 최종 변경 일시")
    @Column(name = "nickname_updated_at")
    private LocalDateTime nicknameUpdatedAt;

    public void enableMember(){
        this.useStatus = Use.Y;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameUpdatedAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void vanMember(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
        this.useStatus = Use.X;
    }

    public void withdraw(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
        this.useStatus = Use.E;
    }
}
