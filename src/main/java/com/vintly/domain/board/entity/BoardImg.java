package com.vintly.domain.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "board_img")
public class BoardImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_img_id")
    private Long boardImgId;

    @Comment("게시글 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Comment("이미지 경로")
    @Column(name = "img_path", nullable = false, columnDefinition = "TEXT")
    private String imgPath;

    @Comment("정렬 순서")
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 1;

    public static BoardImg create(Board board, String imgPath, int sortOrder) {
        BoardImg boardImg = new BoardImg();
        boardImg.board = board;
        boardImg.imgPath = imgPath;
        boardImg.sortOrder = sortOrder;
        return boardImg;
    }
}
