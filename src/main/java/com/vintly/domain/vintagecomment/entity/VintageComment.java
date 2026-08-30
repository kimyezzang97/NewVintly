package com.vintly.domain.vintagecomment.entity;

import com.vintly.domain.BaseEntity;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.vintage.entity.Vintage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // JPA 생성 시간 자동 적용
@Entity(name = "vintage_comment")
public class VintageComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vintage_comment_id")
    private Long vintageCommentId;

    @Comment("VINTAGE 외래 키")
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 관리, DB에서 진짜 필요한 것만 쿼리로 날림.
    @JoinColumn(name = "vintage_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) // FK 제약 X
    private Vintage vintage;

    @Comment("MEMBER 외래 키")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Comment("상위 댓글 (0이면 최상위) default 0")
    @Column(nullable = false)
    private Long parentCommentId;

    @Comment("댓글 내용")
    @Column(nullable = false)
    private String content;

    @Comment("댓글 작성자 닉네임 (탈퇴 시 del_{memberId}로 변경)")
    @Column(name = "author_nickname", nullable = false, length = 30)
    private String authorNickname;

    // ✅ 최상위 댓글
    public static VintageComment createRoot(Vintage vintage, Member member, String content) {
        VintageComment c = new VintageComment();
        c.vintage = vintage;
        c.member = member;
        c.parentCommentId = 0L;
        c.content = content;
        c.authorNickname = member.getNickname();
        return c;
    }

    // 댓글 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }

    // ✅ 대댓글
    public static VintageComment createReply(Vintage vintage, Member member, Long parentCommentId, String content) {
        if (parentCommentId == null || parentCommentId <= 0)
            throw new IllegalArgumentException("parentCommentId는 0보다 커야 합니다.");
        VintageComment c = new VintageComment();
        c.vintage = vintage;
        c.member = member;
        c.parentCommentId = parentCommentId;
        c.content = content;
        c.authorNickname = member.getNickname();
        return c;
    }
}
