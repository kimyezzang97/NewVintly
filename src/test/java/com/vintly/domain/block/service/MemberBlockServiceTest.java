package com.vintly.domain.block.service;

import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.block.repo.MemberBlockRepository;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.interfaces.block.MemberBlockException;
import com.vintly.interfaces.member.MemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberBlockServiceTest {

    @Mock private MemberBlockRepository memberBlockRepository;
    @Mock private MemberRepository memberRepository;
    @InjectMocks private MemberBlockService memberBlockService;

    private static final Long BLOCKER_ID = 1L;
    private static final Long BLOCKED_ID = 2L;

    private Member member(Long memberId, String nickname) {
        return new Member(memberId, memberId + "@test.com", "password", nickname,
                "123456", "ROLE_USER", Use.Y, null, null);
    }

    @Test
    @DisplayName("차단하면 차단한 사람과 차단당한 사람이 저장된다.")
    void blockSavesRelation() {
        // given
        Member blocker = member(BLOCKER_ID, "blockerNick");
        Member blocked = member(BLOCKED_ID, "blockedNick");
        given(memberBlockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).willReturn(false);
        given(memberRepository.findById(BLOCKER_ID)).willReturn(Optional.of(blocker));
        given(memberRepository.findById(BLOCKED_ID)).willReturn(Optional.of(blocked));

        // when
        memberBlockService.block(BLOCKER_ID, BLOCKED_ID);

        // then
        ArgumentCaptor<MemberBlock> captor = ArgumentCaptor.forClass(MemberBlock.class);
        verify(memberBlockRepository).save(captor.capture());
        assertThat(captor.getValue().getBlocker()).isSameAs(blocker);
        assertThat(captor.getValue().getBlocked()).isSameAs(blocked);
    }

    @Test
    @DisplayName("이미 차단한 사람을 다시 차단해도 오류 없이 넘어간다. 차단은 설정이라 멱등이다.")
    void blockIsIdempotent() {
        // given
        given(memberBlockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).willReturn(true);

        // when
        memberBlockService.block(BLOCKER_ID, BLOCKED_ID);

        // then
        verify(memberBlockRepository, never()).save(any(MemberBlock.class));
    }

    @Test
    @DisplayName("동시 요청으로 선체크를 통과해도 유니크 제약 위반은 조용히 넘어간다.")
    void blockSwallowsConstraintViolation() {
        // given
        given(memberBlockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).willReturn(false);
        given(memberRepository.findById(BLOCKER_ID)).willReturn(Optional.of(member(BLOCKER_ID, "blockerNick")));
        given(memberRepository.findById(BLOCKED_ID)).willReturn(Optional.of(member(BLOCKED_ID, "blockedNick")));
        given(memberBlockRepository.save(any(MemberBlock.class)))
                .willThrow(new DataIntegrityViolationException("uk_member_block_blocker_blocked"));

        // when & then - 결과가 "차단됨"으로 같으므로 예외를 밖으로 내보내지 않는다
        memberBlockService.block(BLOCKER_ID, BLOCKED_ID);
    }

    @Test
    @DisplayName("자기 자신은 차단할 수 없다.")
    void cannotBlockSelf() {
        // given

        // when & then
        assertThatThrownBy(() -> memberBlockService.block(BLOCKER_ID, BLOCKER_ID))
                .isInstanceOf(MemberBlockException.SelfBlockException.class);

        verify(memberBlockRepository, never()).save(any(MemberBlock.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원은 차단할 수 없다.")
    void cannotBlockUnknownMember() {
        // given
        given(memberBlockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).willReturn(false);
        given(memberRepository.findById(BLOCKED_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberBlockService.block(BLOCKER_ID, BLOCKED_ID))
                .isInstanceOf(MemberException.MemberNotFoundException.class);

        verify(memberBlockRepository, never()).save(any(MemberBlock.class));
    }

    @Test
    @DisplayName("차단을 해제하면 삭제가 호출된다.")
    void unblockDeletesRelation() {
        // given

        // when
        memberBlockService.unblock(BLOCKER_ID, BLOCKED_ID);

        // then
        verify(memberBlockRepository).deleteByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID);
    }

    @Test
    @DisplayName("차단하지 않은 사람을 해제해도 오류가 나지 않는다. 해제도 멱등이다.")
    void unblockIsIdempotent() {
        // given

        // when & then
        memberBlockService.unblock(BLOCKER_ID, 999L);

        verify(memberBlockRepository).deleteByBlockerIdAndBlockedId(BLOCKER_ID, 999L);
    }

    @Test
    @DisplayName("차단 목록은 상대의 닉네임을 함께 반환한다. 앱이 차단 관리 화면을 그리려면 필요하다.")
    void findMyBlocksReturnsNickname() {
        // given
        MemberBlock block = MemberBlock.create(member(BLOCKER_ID, "blockerNick"), member(BLOCKED_ID, "blockedNick"));
        given(memberBlockRepository.findAllByBlockerId(BLOCKER_ID)).willReturn(List.of(block));

        // when
        List<MemberBlockInfo.Blocked> blocks = memberBlockService.findMyBlocks(BLOCKER_ID);

        // then
        assertThat(blocks).hasSize(1);
        assertThat(blocks.get(0).memberId()).isEqualTo(BLOCKED_ID);
        assertThat(blocks.get(0).nickname()).isEqualTo("blockedNick");
    }

    @Test
    @DisplayName("차단한 회원 ID 목록 조회는 리포지토리에 그대로 위임한다. 조회 필터가 쓴다.")
    void findBlockedIdsDelegates() {
        // given
        given(memberBlockRepository.findBlockedIdsByBlockerId(BLOCKER_ID)).willReturn(List.of(2L, 3L));

        // when
        List<Long> blockedIds = memberBlockService.findBlockedIds(BLOCKER_ID);

        // then
        assertThat(blockedIds).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("댓글 작성 차단 검사는 글쓴이가 나를 차단했는지를 본다. 방향을 뒤집으면 안 된다.")
    void checksWhetherAuthorBlockedMe() {
        // given - 글쓴이(2)가 나(1)를 차단한 상황
        given(memberBlockRepository.existsByBlockerIdAndBlockedId(BLOCKED_ID, BLOCKER_ID)).willReturn(true);

        // when
        boolean blockedByAuthor = memberBlockService.isBlockedBy(BLOCKER_ID, BLOCKED_ID);

        // then
        assertThat(blockedByAuthor).isTrue();
        verify(memberBlockRepository).existsByBlockerIdAndBlockedId(BLOCKED_ID, BLOCKER_ID);
        verify(memberBlockRepository, never()).existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID);
    }
}
