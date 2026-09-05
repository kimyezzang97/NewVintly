package com.vintly.domain.block.entity;

import com.vintly.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 차단. 차단한 사람의 화면에서만 상대의 글과 댓글이 사라지는 단방향 관계다.
 *
 * 신고(report)와 달리 감사 기록이 아니라 개인 설정이라, 어느 쪽이 탈퇴하든 함께 삭제한다.
 * updatedAt은 두지 않는다. 차단은 수정되지 않고 생기거나 사라진다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "member_block")
@Table(name = "member_block", uniqueConstraints = @UniqueConstraint(
        name = "uk_member_block_blocker_blocked",
        columnNames = {"blocker_id", "blocked_id"}))
public class MemberBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id")
    private Long blockId;

    @Comment("차단한 회원 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member blocker;

    @Comment("차단당한 회원 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member blocked;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static MemberBlock create(Member blocker, Member blocked) {
        MemberBlock block = new MemberBlock();
        block.blocker = blocker;
        block.blocked = blocked;
        return block;
    }
}
