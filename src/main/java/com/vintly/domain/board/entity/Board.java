package com.vintly.domain.board.entity;

import com.vintly.domain.BaseEntity;
import com.vintly.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Comment("작성자 ID (탈퇴 시 orphaned)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member member;

    @Comment("작성 당시 닉네임")
    @Column(name = "author_nickname", nullable = false, length = 30)
    private String authorNickname;

    @Comment("제목")
    @Column(nullable = false)
    private String title;

    @Comment("본문 내용")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Comment("조회수")
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    public static Board create(Member member, String title, String content) {
        Board board = new Board();
        board.member = member;
        board.authorNickname = member.getNickname();
        board.title = title;
        board.content = content;
        board.viewCount = 0;
        return board;
    }

    public void updateInfo(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
