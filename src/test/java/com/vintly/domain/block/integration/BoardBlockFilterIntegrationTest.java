package com.vintly.domain.block.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.board.dto.BoardInfo;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.board.service.BoardService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 게시글 목록의 차단 필터 통합 테스트.
 *
 * 설계 문서(docs/design/block.md)가 경고한 함정 두 가지를 여기서 고정한다.
 *   함정 1 — NOT IN 은 NULL 을 걸러낸다. 탈퇴 회원 글이 통째로 사라지면 안 된다.
 *   함정 2 — 차단이 0건이면 조건 자체를 붙이면 안 된다. NOT IN () 은 깨진다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BoardBlockFilterIntegrationTest extends TestContainerConfig {

    @Autowired private BoardService boardService;
    @Autowired private BoardRepository boardRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final Pageable PAGE = PageRequest.of(0, 20);

    private Member author;
    private Member innocent;
    private Member withdrawn;
    private Long authorBoardId;
    private Long innocentBoardId;
    private Long withdrawnBoardId;

    @BeforeEach
    void setUp() {
        author = memberRepository.save(newMember("blk-author@test.local", "blkAuthor"));
        innocent = memberRepository.save(newMember("blk-innocent@test.local", "blkInnocent"));
        withdrawn = memberRepository.save(newMember("blk-withdrawn@test.local", "blkWithdrawn"));

        authorBoardId = boardRepository.save(Board.create(author, "차단 대상 글", "본문")).getBoardId();
        innocentBoardId = boardRepository.save(Board.create(innocent, "멀쩡한 글", "본문")).getBoardId();
        withdrawnBoardId = boardRepository.save(Board.create(withdrawn, "탈퇴자 글", "본문")).getBoardId();

        entityManager.flush();

        // 탈퇴를 재현한다. member 행을 지우면 board.member_id 는 orphan 으로 남는다.
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", withdrawn.getMemberId());
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    private List<Long> boardIdsOf(List<Long> blockedIds) {
        return boardService.getBoardList(null, PAGE, blockedIds)
                .getContent().stream()
                .map(BoardInfo.BoardSummary::boardId)
                .toList();
    }

    @Test
    @DisplayName("차단한 회원의 글은 목록에서 사라진다.")
    void blockedAuthorBoardIsHidden() {
        // given

        // when
        List<Long> boardIds = boardIdsOf(List.of(author.getMemberId()));

        // then
        assertThat(boardIds).doesNotContain(authorBoardId);
    }

    @Test
    @DisplayName("차단하지 않은 회원의 글은 그대로 보인다.")
    void otherBoardsRemainVisible() {
        // given

        // when
        List<Long> boardIds = boardIdsOf(List.of(author.getMemberId()));

        // then
        assertThat(boardIds).contains(innocentBoardId);
    }

    @Test
    @DisplayName("함정 1 — 탈퇴 회원의 글은 차단과 무관하므로 목록에 남아야 한다.")
    void withdrawnAuthorBoardSurvivesFiltering() {
        // given - 작성자가 이미 탈퇴해 member 행이 없는 글

        // when
        List<Long> boardIds = boardIdsOf(List.of(author.getMemberId()));

        // then - NOT IN 이 NULL 을 걸러내면 이 글이 통째로 사라진다
        assertThat(boardIds).contains(withdrawnBoardId);
    }

    @Test
    @DisplayName("함정 2 — 차단이 없으면 모든 글이 보인다.")
    void noBlockShowsEverything() {
        // given

        // when - 빈 목록으로 NOT IN () 을 만들면 SQL 이 깨진다
        List<Long> boardIds = boardIdsOf(List.of());

        // then
        assertThat(boardIds).contains(authorBoardId, innocentBoardId, withdrawnBoardId);
    }

    @Test
    @DisplayName("전체 건수도 차단을 반영한다. 페이지 정보가 어긋나면 안 된다.")
    void totalCountReflectsBlocking() {
        // given
        long totalWithoutBlock = boardService.getBoardList(null, PAGE, List.of()).getTotalElements();

        // when
        long totalWithBlock = boardService.getBoardList(null, PAGE, List.of(author.getMemberId()))
                .getTotalElements();

        // then
        assertThat(totalWithBlock).isEqualTo(totalWithoutBlock - 1);
    }
}
