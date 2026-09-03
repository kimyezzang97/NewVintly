package com.vintly.domain.member.integration;

import com.vintly.TestContainerConfig;
import com.vintly.application.member.MemberFacade;
import com.vintly.domain.board.entity.Board;
import com.vintly.domain.board.entity.BoardComment;
import com.vintly.domain.board.entity.BoardLike;
import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardLikeRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.board.service.BoardCommentService;
import com.vintly.domain.board.service.BoardLikeService;
import com.vintly.domain.mail.service.MailService;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.member.service.MemberService;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.domain.vintage.entity.Vintage;
import com.vintly.domain.vintage.repo.VintageRepository;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.domain.vintagelike.entity.VintageLike;
import com.vintly.domain.vintagelike.repo.VintageLikeRepository;
import com.vintly.domain.vintagecomment.service.VintageCommentService;
import com.vintly.domain.vintagelike.service.VintageLikeService;
import com.vintly.interfaces.member.MemberException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 회원 탈퇴(하드 삭제) 통합 테스트.
 *
 * 격리 방식 — 클래스에 {@link Transactional}을 걸어 각 테스트가 끝나면 롤백된다.
 * 다만 Redis는 트랜잭션 대상이 아니므로 {@link #clearRedisKeys()}에서 직접 지운다.
 *
 * 주의 1. 벌크 쿼리와 1차 캐시 — 탈퇴 로직은 전부 벌크 {@code @Modifying} 쿼리라
 * 영속성 컨텍스트를 우회한다. 같은 트랜잭션에서 엔티티로 읽으면 1차 캐시의 변경 전 값이 나와
 * "익명화가 안 됐는데 통과"하는 가짜 성공이 된다. 그래서 검증은 {@link JdbcTemplate}으로 DB를 직접 읽는다
 * (같은 커넥션을 쓰므로 커밋 전 데이터도 보인다).
 *
 * 주의 2. 운영 스키마 재현 — {@code ddl-auto=create-drop}은 엔티티에서 DDL을 만들기 때문에
 * 운영 DB에 실제로 걸려 있는 {@code ON UPDATE CURRENT_TIMESTAMP}가 생성되지 않는다. 그 상태로 두면
 * 익명화·조회수 쿼리에서 {@code SET x.updatedAt = x.updatedAt}을 지워도 테스트가 통과한다.
 * {@link #replicateProductionSchema()}가 그 속성을 붙여 회귀를 실제로 잡도록 한다.
 * (실제 DB에 직접 붙여 테스트한다면 이 메서드는 불필요하다 — 이미 스키마에 있으므로.)
 *
 * 실행 조건 — Docker 가 필요하다. DB/Redis 는 {@link TestContainerConfig}가 컨테이너로 띄우고
 * 접속 정보를 주입하며, 나머지 설정은 {@code src/test/resources/application-test.yml}(커밋됨)에 있다.
 * 별도 로컬 설정 없이 클론 직후 {@code ./gradlew test}로 바로 돌아간다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberWithdrawIntegrationTest extends TestContainerConfig {

    private static final String PASSWORD = "Passw0rd!23";

    @Autowired private MemberService memberService;
    @Autowired private MemberFacade memberFacade;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardCommentRepository boardCommentRepository;
    @Autowired private BoardLikeRepository boardLikeRepository;
    @Autowired private VintageRepository vintageRepository;
    @Autowired private VintageCommentRepository vintageCommentRepository;
    @Autowired private VintageLikeRepository vintageLikeRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private BoardLikeService boardLikeService;
    @Autowired private VintageLikeService vintageLikeService;
    @Autowired private BoardCommentService boardCommentService;
    @Autowired private VintageCommentService vintageCommentService;
    @Autowired private EntityManager entityManager;

    @MockitoBean private MailService mailService;

    @BeforeEach
    void replicateProductionSchema() {
        jdbcTemplate.execute("ALTER TABLE board MODIFY COLUMN updated_at DATETIME(6) NOT NULL "
                + "DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");
        jdbcTemplate.execute("ALTER TABLE board_comment MODIFY COLUMN updated_at DATETIME(6) NOT NULL "
                + "DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)");
    }

    /** Redis는 롤백되지 않으므로 직접 정리한다. */
    @AfterEach
    void clearRedisKeys() {
        redisTemplate.delete(redisTemplate.keys("refresh:*@test.local"));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("탈퇴하면 member 행이 물리 삭제되고, 글/댓글은 익명화된 채 남으며, 좋아요는 삭제된다.")
    void withdrawHardDeletesMemberAndAnonymizesContent() {
        // Arrange
        Fixture f = createFixture("hard-delete@test.local", "hardDelete");

        // Act
        memberService.withdrawMember(load(f), PASSWORD);
        entityManager.flush();

        // Assert
        String del = "del_" + f.memberId();
        assertThat(count("SELECT COUNT(*) FROM member WHERE member_id = ?", f.memberId())).isZero();

        assertThat(scalar("SELECT author_nickname FROM board WHERE board_id = ?", f.boardId())).isEqualTo(del);
        assertThat(scalar("SELECT author_nickname FROM board_comment WHERE board_comment_id = ?", f.boardCommentId()))
                .isEqualTo(del);
        assertThat(scalar("SELECT author_nickname FROM vintage_comment WHERE vintage_comment_id = ?", f.vintageCommentId()))
                .isEqualTo(del);

        // 매장 댓글은 작성자 링크를 끊는다 (컬럼이 NOT NULL이면 1048로 실패하므로 스키마 회귀도 함께 잡힌다)
        assertThat(scalar("SELECT member_id FROM vintage_comment WHERE vintage_comment_id = ?", f.vintageCommentId()))
                .isNull();

        assertThat(count("SELECT COUNT(*) FROM board_like WHERE member_id = ?", f.memberId())).isZero();
        assertThat(count("SELECT COUNT(*) FROM vintage_like WHERE member_id = ?", f.memberId())).isZero();
    }

    @Test
    @DisplayName("익명화는 '수정'이 아니므로 글/댓글의 updated_at을 건드리지 않는다.")
    void anonymizationPreservesUpdatedAt() {
        // Arrange
        Fixture f = createFixture("keep-updated-at@test.local", "keepUpdated");
        LocalDateTime boardUpdatedAt = timestamp("SELECT updated_at FROM board WHERE board_id = ?", f.boardId());
        LocalDateTime commentUpdatedAt =
                timestamp("SELECT updated_at FROM board_comment WHERE board_comment_id = ?", f.boardCommentId());

        // Act
        memberService.withdrawMember(load(f), PASSWORD);
        entityManager.flush();

        // Assert
        assertThat(timestamp("SELECT updated_at FROM board WHERE board_id = ?", f.boardId()))
                .isEqualTo(boardUpdatedAt);
        assertThat(timestamp("SELECT updated_at FROM board_comment WHERE board_comment_id = ?", f.boardCommentId()))
                .isEqualTo(commentUpdatedAt);
    }

    @Test
    @DisplayName("조회수 증가는 게시글의 updated_at을 갱신하지 않는다.")
    void incrementViewCountPreservesUpdatedAt() {
        // Arrange
        Fixture f = createFixture("view-count@test.local", "viewCount");
        LocalDateTime before = timestamp("SELECT updated_at FROM board WHERE board_id = ?", f.boardId());

        // Act
        boardRepository.incrementViewCount(f.boardId());
        boardRepository.incrementViewCount(f.boardId());

        // Assert
        assertThat(count("SELECT view_count FROM board WHERE board_id = ?", f.boardId())).isEqualTo(2);
        assertThat(timestamp("SELECT updated_at FROM board WHERE board_id = ?", f.boardId())).isEqualTo(before);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 탈퇴가 중단되어 회원과 좋아요가 모두 남는다.")
    void withdrawWithWrongPasswordChangesNothing() {
        // Arrange
        Fixture f = createFixture("wrong-password@test.local", "wrongPw");

        // Act & Assert
        assertThrows(MemberException.PasswordNotMatchException.class,
                () -> memberService.withdrawMember(load(f), "wrong-password"));

        assertThat(count("SELECT COUNT(*) FROM member WHERE member_id = ?", f.memberId())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM board_like WHERE member_id = ?", f.memberId())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM vintage_like WHERE member_id = ?", f.memberId())).isEqualTo(1);
        assertThat(scalar("SELECT author_nickname FROM board WHERE board_id = ?", f.boardId()))
                .isEqualTo(f.nickname());
    }

    @Test
    @DisplayName("탈퇴 시 다른 회원의 글/댓글/좋아요는 건드리지 않는다.")
    void withdrawDoesNotTouchOtherMembersData() {
        // Arrange
        Fixture leaving = createFixture("leaving@test.local", "leaving");
        Fixture staying = createFixture("staying@test.local", "staying");

        // Act
        memberService.withdrawMember(load(leaving), PASSWORD);
        entityManager.flush();

        // Assert
        assertThat(count("SELECT COUNT(*) FROM member WHERE member_id = ?", staying.memberId())).isEqualTo(1);
        assertThat(scalar("SELECT author_nickname FROM board WHERE board_id = ?", staying.boardId()))
                .isEqualTo(staying.nickname());
        assertThat(scalar("SELECT author_nickname FROM vintage_comment WHERE vintage_comment_id = ?",
                staying.vintageCommentId())).isEqualTo(staying.nickname());
        assertThat(count("SELECT COUNT(*) FROM board_like WHERE member_id = ?", staying.memberId())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM vintage_like WHERE member_id = ?", staying.memberId())).isEqualTo(1);
    }

    @Test
    @DisplayName("탈퇴하면 Redis에 남아있던 refresh 토큰도 함께 폐기된다.")
    void withdrawDeletesRefreshToken() {
        // Arrange
        Fixture f = createFixture("refresh-token@test.local", "refreshTk");
        String redisKey = "refresh:" + f.email();
        redisTemplate.opsForValue().set(redisKey, "stored-refresh-token");

        // Act
        memberFacade.withdrawMember(f.email(), PASSWORD);
        entityManager.flush();

        // Assert
        assertThat(count("SELECT COUNT(*) FROM member WHERE member_id = ?", f.memberId())).isZero();
        assertThat(redisTemplate.hasKey(redisKey)).isFalse();
    }

    @Test
    @DisplayName("탈퇴한 회원의 access 토큰이 남아 있어도 좋아요·댓글은 남길 수 없다.")
    void deletedMemberCannotWriteAnything() {
        // Arrange : 탈퇴 직후, 아직 만료되지 않은 토큰으로 요청이 들어오는 상황
        Fixture f = createFixture("still-has-token@test.local", "hasToken");
        Long boardId = f.boardId();
        Long vintageId = f.vintageId();
        Long deletedMemberId = f.memberId();

        memberService.withdrawMember(load(f), PASSWORD);
        entityManager.flush();
        entityManager.clear();

        authenticateAs(deletedMemberId, f.email());

        // Act & Assert : JWTFilter는 DB를 보지 않으므로 각 서비스가 스스로 회원 존재를 확인해야 한다
        assertThrows(MemberException.MemberNotFoundException.class,
                () -> boardLikeService.like(boardId));
        assertThrows(MemberException.MemberNotFoundException.class,
                () -> vintageLikeService.like(vintageId));
        assertThrows(MemberException.MemberNotFoundException.class,
                () -> boardCommentService.create(boardId, deletedMemberId, 0L, "comment"));
        assertThrows(MemberException.MemberNotFoundException.class,
                () -> vintageCommentService.create(vintageId, deletedMemberId, 0L, "comment"));

        assertThat(count("SELECT COUNT(*) FROM board_like WHERE member_id = ?", deletedMemberId)).isZero();
        assertThat(count("SELECT COUNT(*) FROM vintage_like WHERE member_id = ?", deletedMemberId)).isZero();
    }

    /** 탈퇴 전에 발급된 토큰으로 인증된 상태를 흉내낸다 (JWTFilter는 DB 조회 없이 클레임만으로 인증한다). */
    private void authenticateAs(Long memberId, String email) {
        Member claimsOnly = new Member(memberId, email, null, null, null, "ROLE_USER", null, null, null);
        CustomUserDetails principal = new CustomUserDetails(claimsOnly);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    // --- fixture ---

    private record Fixture(String email, String nickname, Long memberId, Long boardId,
                           Long boardCommentId, Long vintageId, Long vintageCommentId) {}

    /** 탈퇴 대상 회원을 영속성 컨텍스트가 비워진 상태에서 새로 조회한다. */
    private Member load(Fixture f) {
        return memberRepository.findByEmail(f.email()).orElseThrow();
    }

    /**
     * 회원 1명과 그 회원의 게시글·댓글·좋아요·매장댓글·매장좋아요를 한 벌씩 만든다.
     *
     * flush로 INSERT를 내보낸 뒤 clear로 영속성 컨텍스트를 비우는 것이 핵심이다. 비우지 않으면
     * 탈퇴가 {@code em.remove(member)}를 호출하는 순간, 같은 컨텍스트에 남아 있는 Board/BoardLike 등이
     * 여전히 그 Member를 참조하고 있어 flush 시 TransientObjectException이 난다. 운영에서는 요청마다
     * 영속성 컨텍스트가 분리되어 발생하지 않는, 테스트 전용 함정이다.
     */
    private Fixture createFixture(String email, String nickname) {
        Member member = memberRepository.save(new Member(
                null, email, passwordEncoder.encode(PASSWORD), nickname,
                "123456", "ROLE_USER", Use.Y, null, null));

        Board board = boardRepository.save(Board.create(member, "title", "content"));
        BoardComment boardComment = boardCommentRepository.save(
                BoardComment.createRoot(board, member, "board comment"));
        boardLikeRepository.save(BoardLike.create(board, member));

        Vintage vintage = vintageRepository.save(Vintage.create(
                "vintage-" + nickname, "서울특별시", "중구", "세종대로 110",
                new BigDecimal("37.566500"), new BigDecimal("126.978000"), null));
        VintageComment vintageComment = vintageCommentRepository.save(
                VintageComment.createRoot(vintage, member, "vintage comment"));
        vintageLikeRepository.save(VintageLike.create(vintage, member));

        entityManager.flush();
        Fixture fixture = new Fixture(email, nickname, member.getMemberId(), board.getBoardId(),
                boardComment.getBoardCommentId(), vintage.getVintageId(), vintageComment.getVintageCommentId());
        entityManager.clear();
        return fixture;
    }

    // --- 1차 캐시를 우회해 DB 실제 값을 읽는 헬퍼 ---

    private String scalar(String sql, Object arg) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, arg);
        assertThat(rows).hasSize(1);
        Object value = rows.get(0).values().iterator().next();
        return value == null ? null : String.valueOf(value);
    }

    private long count(String sql, Object arg) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, arg);
        return value == null ? 0L : value;
    }

    private LocalDateTime timestamp(String sql, Object arg) {
        return jdbcTemplate.queryForObject(sql, LocalDateTime.class, arg);
    }
}
