package com.vintly.domain.block.integration;

import com.vintly.TestContainerConfig;
import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.block.repo.MemberBlockRepository;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 사용자 차단 리포지토리 통합 테스트.
 *
 * 실행에는 Docker가 필요하다. 격리는 클래스 레벨 Transactional 롤백.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberBlockRepositoryIntegrationTest extends TestContainerConfig {

    @Autowired private MemberBlockRepository memberBlockRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Member blocker;
    private Member blocked;
    private Member other;

    @BeforeEach
    void setUp() {
        blocker = memberRepository.save(newMember("blocker@test.local", "blockerNick"));
        blocked = memberRepository.save(newMember("blocked@test.local", "blockedNick"));
        other = memberRepository.save(newMember("other@test.local", "otherNick"));
        entityManager.flush();
        entityManager.clear();
    }

    private Member newMember(String email, String nickname) {
        return new Member(null, email, "encodedPassword", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("차단을 저장하면 차단 여부 조회로 확인된다.")
    void savedBlockIsFound() {
        // given
        MemberBlock block = MemberBlock.create(blocker, blocked);

        // when
        memberBlockRepository.save(block);
        entityManager.clear();

        // then
        assertThat(memberBlockRepository.existsByBlockerIdAndBlockedId(
                blocker.getMemberId(), blocked.getMemberId())).isTrue();
    }

    @Test
    @DisplayName("차단은 단방향이다. 내가 차단해도 상대의 차단 목록에는 없다.")
    void blockIsOneWay() {
        // given
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        entityManager.clear();

        // when
        boolean reverse = memberBlockRepository.existsByBlockerIdAndBlockedId(
                blocked.getMemberId(), blocker.getMemberId());

        // then
        assertThat(reverse).isFalse();
    }

    @Test
    @DisplayName("같은 사람을 두 번 차단하면 유니크 제약에 걸린다.")
    void duplicateBlockIsRejectedByUniqueConstraint() {
        // given
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));

        // when & then
        assertThatThrownBy(() -> memberBlockRepository.save(MemberBlock.create(blocker, blocked)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("차단한 회원 ID 목록을 조회한다. 조회 필터가 이 목록을 쓴다.")
    void findsBlockedIds() {
        // given
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        memberBlockRepository.save(MemberBlock.create(blocker, other));
        entityManager.clear();

        // when
        List<Long> blockedIds = memberBlockRepository.findBlockedIdsByBlockerId(blocker.getMemberId());

        // then
        assertThat(blockedIds).containsExactlyInAnyOrder(blocked.getMemberId(), other.getMemberId());
    }

    @Test
    @DisplayName("차단이 없으면 빈 목록을 반환한다. 이때 조회 조건을 붙이면 안 된다.")
    void returnsEmptyListWhenNothingBlocked() {
        // given

        // when
        List<Long> blockedIds = memberBlockRepository.findBlockedIdsByBlockerId(blocker.getMemberId());

        // then
        assertThat(blockedIds).isEmpty();
    }

    @Test
    @DisplayName("차단을 해제하면 행이 삭제된다.")
    void deleteRemovesBlock() {
        // given
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        entityManager.flush();

        // when
        memberBlockRepository.deleteByBlockerIdAndBlockedId(blocker.getMemberId(), blocked.getMemberId());
        entityManager.flush();

        // then
        assertThat(countBlocksOf(blocker)).isZero();
    }

    @Test
    @DisplayName("차단 해제 시 다른 사람에 대한 차단은 남는다.")
    void deleteKeepsOtherBlocks() {
        // given
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        memberBlockRepository.save(MemberBlock.create(blocker, other));
        entityManager.flush();

        // when
        memberBlockRepository.deleteByBlockerIdAndBlockedId(blocker.getMemberId(), blocked.getMemberId());
        entityManager.flush();

        // then
        assertThat(memberBlockRepository.findBlockedIdsByBlockerId(blocker.getMemberId()))
                .containsExactly(other.getMemberId());
    }

    @Test
    @DisplayName("탈퇴 시 이 회원이 관련된 차단은 차단한 쪽이든 당한 쪽이든 모두 삭제된다.")
    void deleteAllByMemberRemovesBothDirections() {
        // given - blocker가 blocked를 차단하고, other가 blocked를 차단한 상태
        memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        memberBlockRepository.save(MemberBlock.create(other, blocked));
        memberBlockRepository.save(MemberBlock.create(blocked, other));
        entityManager.flush();

        // when - blocked가 탈퇴
        memberBlockRepository.deleteAllByMemberId(blocked.getMemberId());
        entityManager.flush();

        // then - blocked가 관련된 3건이 모두 사라진다
        Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM member_block", Integer.class);
        assertThat(remaining).isZero();
    }

    private int countBlocksOf(Member member) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM member_block WHERE blocker_id = ?", Integer.class, member.getMemberId());
        return count != null ? count : 0;
    }
}
