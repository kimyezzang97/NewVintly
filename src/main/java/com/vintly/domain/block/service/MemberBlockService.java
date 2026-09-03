package com.vintly.domain.block.service;

import com.vintly.domain.block.dto.MemberBlockInfo;
import com.vintly.domain.block.entity.MemberBlock;
import com.vintly.domain.block.repo.MemberBlockRepository;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.interfaces.block.MemberBlockException;
import com.vintly.interfaces.member.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberBlockService {

    private final MemberBlockRepository memberBlockRepository;
    private final MemberRepository memberRepository;

    /**
     * 차단. 이미 차단한 상대면 아무것도 하지 않는다.
     *
     * 차단은 신고와 달리 개인 설정이라 "이미 그 상태"가 오류일 이유가 없다. 그래서 중복 요청도,
     * 동시 요청이 선체크를 함께 통과해 유니크 제약에 걸리는 경우도 성공으로 처리한다. 어느 쪽이든
     * 호출자가 원한 결과("이 사람을 차단한 상태")는 이미 이뤄져 있다.
     */
    @Transactional(rollbackFor = Exception.class)
    public void block(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new MemberBlockException.SelfBlockException();
        }

        if (memberBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }

        Member blocked = memberRepository.findById(blockedId)
                .orElseThrow(MemberException.MemberNotFoundException::new);
        Member blocker = memberRepository.findById(blockerId)
                .orElseThrow(MemberException.MemberNotFoundException::new);

        try {
            memberBlockRepository.save(MemberBlock.create(blocker, blocked));
        } catch (DataIntegrityViolationException e) {
            log.debug("동시 차단 요청으로 유니크 제약 위반 - blocker: {}, blocked: {}", blockerId, blockedId);
        }
    }

    // 차단하지 않은 상대를 해제해도 오류가 아니다. 결과("차단하지 않은 상태")가 같다.
    @Transactional(rollbackFor = Exception.class)
    public void unblock(Long blockerId, Long blockedId) {
        memberBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public List<MemberBlockInfo.Blocked> findMyBlocks(Long blockerId) {
        return memberBlockRepository.findAllByBlockerId(blockerId).stream()
                .map(MemberBlockInfo.Blocked::from)
                .toList();
    }

    // 조회 필터가 쓰는 목록. 비어 있으면 호출부에서 조건을 붙이지 않아야 한다 (NOT IN () 은 깨진다).
    @Transactional(readOnly = true)
    public List<Long> findBlockedIds(Long blockerId) {
        return memberBlockRepository.findBlockedIdsByBlockerId(blockerId);
    }

    /**
     * 내가 상대에게 차단당했는지 확인한다. 댓글 작성 검사에서 쓴다.
     *
     * 방향에 주의할 것. 막아야 하는 것은 "내가 차단한 사람"이 아니라 "나를 차단한 사람의 글"이므로
     * blocker 자리에 상대가, blocked 자리에 내가 들어간다.
     */
    @Transactional(readOnly = true)
    public boolean isBlockedBy(Long myId, Long otherId) {
        return memberBlockRepository.existsByBlockerIdAndBlockedId(otherId, myId);
    }
}
