package com.vintly.domain.board.service;

import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.member.repo.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardCommentServiceTest {

    @Mock
    private BoardCommentRepository boardCommentRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BoardCommentService boardCommentService;

    @Test
    @DisplayName("board 삭제 시 해당 board의 모든 댓글이 삭제된다.")
    void shouldDeleteAllCommentsByBoardId() {
        // Arrange
        Long boardId = 1L;

        // Act
        boardCommentService.deleteAllByBoardId(boardId);

        // Assert
        verify(boardCommentRepository).deleteAllByBoardId(boardId);
    }
}
