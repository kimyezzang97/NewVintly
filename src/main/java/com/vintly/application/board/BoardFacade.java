package com.vintly.application.board;

import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardImg;
import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.domain.board.service.BoardImgService;
import com.vintly.domain.board.service.BoardLikeService;
import com.vintly.domain.board.service.BoardService;
import com.vintly.domain.img.service.ImgService;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.MemberService;
import com.vintly.infra.util.SecurityUtil;
import com.vintly.interfaces.board.BoardException;
import com.vintly.interfaces.board.BoardRequest;
import com.vintly.interfaces.board.BoardResponse;
import com.vintly.interfaces.member.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BoardFacade {

    private final BoardService boardService;
    private final BoardImgService boardImgService;
    private final BoardCommentService boardCommentService;
    private final BoardLikeService boardLikeService;
    private final ImgService imgService;
    private final MemberService memberService;

    // 게시글 목록 조회 (검색)
    @Transactional(readOnly = true)
    public Page<BoardResponse.BoardList> getBoardList(String keyword, Pageable pageable) {
        return boardService.getBoardList(keyword, pageable)
                .map(BoardResponse.BoardList::from);
    }

    // 게시글 상세 조회
    @Transactional(rollbackFor = Exception.class)
    public BoardResponse.BoardDetail getBoard(Long boardId) {
        Board board = boardService.findById(boardId)
                .orElseThrow(BoardException.BoardNotFoundException::new);

        boardService.incrementViewCount(boardId);

        List<BoardInfo.BoardImgInfo> imgList = boardImgService.findImgListByBoardId(boardId);
        List<BoardInfo.Comment> comments = boardCommentService.findCommentsByBoardId(boardId);

        var likeStatus = boardLikeService.getStatus(boardId);

        BoardInfo.BoardDetail detail = new BoardInfo.BoardDetail(
                board.getBoardId(),
                board.getMember() != null ? board.getMember().getMemberId() : null,
                board.getAuthorNickname(),
                board.getTitle(),
                board.getContent(),
                board.getViewCount(),
                likeStatus.likeCount(),
                likeStatus.liked(),
                imgList,
                comments,
                board.getCreatedAt(),
                board.getUpdatedAt()
        );

        return BoardResponse.BoardDetail.from(detail);
    }

    // 게시글 생성
    @Transactional(rollbackFor = Exception.class)
    public void createBoard(BoardRequest.CreateBoard request) {
        String email = SecurityUtil.getCurrentEmail();
        Member member = memberService.findByEmail(email)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        Board board = boardService.createBoard(member, request.title(), request.content());

        List<MultipartFile> images = request.images();
        if (!CollectionUtils.isEmpty(images)) {
            List<String> imgUrls = imgService.uploadImgList(images, "board");
            boardImgService.saveImgList(imgUrls, board);
        }
    }

    // 게시글 수정
    @Transactional(rollbackFor = Exception.class)
    public void updateBoard(Long boardId, BoardRequest.UpdateBoard request) {
        Board board = boardService.findById(boardId)
                .orElseThrow(BoardException.BoardNotFoundException::new);

        String email = SecurityUtil.getCurrentEmail();
        Member member = memberService.findByEmail(email)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        if (board.getMember() == null || !board.getMember().getMemberId().equals(member.getMemberId())) {
            throw new BoardException.BoardForbiddenException();
        }

        boardService.updateBoard(board, request.title(), request.content());
        updateBoardImg(board, request);
    }

    // 게시글 삭제 (작성자)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoard(Long boardId) {
        Board board = boardService.findById(boardId)
                .orElseThrow(BoardException.BoardNotFoundException::new);

        String email = SecurityUtil.getCurrentEmail();
        Member member = memberService.findByEmail(email)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        boardService.deleteByWriter(board, member);
    }

    // 게시글 삭제 (관리자)
    @Transactional(rollbackFor = Exception.class)
    public void deleteBoardByAdmin(Long boardId) {
        Board board = boardService.findById(boardId)
                .orElseThrow(BoardException.BoardNotFoundException::new);

        boardService.deleteByAdmin(board);
    }

    private void updateBoardImg(Board board, BoardRequest.UpdateBoard request) {
        List<Long> remainImgIdList = Optional.ofNullable(request.remainImgIdList())
                .orElse(Collections.emptyList());

        List<BoardImg> existingImgList = boardImgService.findImgEntityListByBoardId(board.getBoardId());

        List<BoardImg> toDelete = existingImgList.stream()
                .filter(img -> !remainImgIdList.contains(img.getBoardImgId()))
                .toList();

        List<String> deleteImgPaths = toDelete.stream()
                .map(BoardImg::getImgPath)
                .toList();

        boardImgService.deleteAll(toDelete);
        imgService.deleteImgList(deleteImgPaths);

        List<MultipartFile> newImages = request.imgList();
        if (!CollectionUtils.isEmpty(newImages)) {
            List<String> uploadedPaths = imgService.uploadImgList(newImages, "board");
            boardImgService.saveImgList(uploadedPaths, board);
        }
    }
}
