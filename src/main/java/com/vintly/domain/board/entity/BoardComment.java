package com.vintly.domain.board.entity;

import com.vintly.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "board_comment")
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_comment_id")
    private Long boardCommentId;

    @Comment("게시글 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Comment("작성자 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Comment("작성 당시 닉네임")
    @Column(name = "author_nickname", nullable = false, length = 30)
    private String authorNickname;

    @Comment("상위 댓글 ID (0이면 최상위)")
    @Column(name = "parent_id", nullable = false)
    private Long parentId = 0L;

    @Comment("댓글 내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("[삭제자] 작성자: W, 관리자: S, 정상: N")
    @Enumerated(EnumType.STRING)
    @Column(name = "del_status", nullable = false, length = 1)
    private DelStatus delStatus = DelStatus.N;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
