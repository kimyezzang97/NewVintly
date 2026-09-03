package com.vintly.application.report;

import com.vintly.domain.board.repo.BoardCommentRepository;
import com.vintly.domain.board.repo.BoardRepository;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportTargetType;
import com.vintly.domain.report.dto.ReportInfo;
import com.vintly.domain.report.service.ReportService;
import com.vintly.domain.vintagecomment.entity.VintageComment;
import com.vintly.domain.vintagecomment.repo.VintageCommentRepository;
import com.vintly.interfaces.report.ReportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReportFacade {

    private final ReportService reportService;
    private final BoardRepository boardRepository;
    private final BoardCommentRepository boardCommentRepository;
    private final VintageCommentRepository vintageCommentRepository;

    /**
     * 신고 접수. 대상 존재 확인과 자기 콘텐츠 판정을 거쳐 {@link ReportService}에 넘긴다.
     *
     * 대상 도메인({@code board}, {@code board_comment}, {@code vintage_comment})을 알아야 하는
     * 판정이라 도메인 서비스가 아니라 여기서 조합한다.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long report(Long reporterId, ReportTargetType targetType, Long targetId,
                       ReportReason reason, String detail) {
        if (findAuthorId(targetType, targetId).filter(reporterId::equals).isPresent()) {
            throw new ReportException.SelfReportException();
        }

        return reportService.create(reporterId, targetType, targetId, reason, detail);
    }

    @Transactional(readOnly = true)
    public List<ReportInfo.My> findMyReports(Long reporterId) {
        return reportService.findMyReports(reporterId);
    }

    /**
     * 대상의 작성자 ID를 찾는다. 대상이 없으면 예외를 던지므로 존재 확인을 겸한다.
     *
     * 비어 있는 {@link Optional}은 "대상이 없다"가 아니라 "작성자가 없다"는 뜻이다.
     * {@code vintage_comment}는 작성자 탈퇴 시 {@code member_id}가 null이 되기 때문이다. 이 경우
     * 댓글 자체는 남아 있으므로 신고할 수 있어야 한다.
     *
     * 대상이 3종뿐이라 {@code switch}로 둔다. 늘어나면 {@link ReportTargetType}에 확인 전략을
     * 붙이는 형태가 자연스럽다.
     *
     * 존재 확인만 필요한 자리에 {@code findById}로 엔티티 전체를 읽는 것은, 작성자 ID를 함께
     * 얻어야 해서다. LAZY 프록시에서 식별자를 꺼내는 것은 추가 쿼리를 일으키지 않으므로 대상당
     * 한 번의 조회로 끝난다.
     */
    private Optional<Long> findAuthorId(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case BOARD -> Optional.of(boardRepository.findById(targetId)
                    .orElseThrow(ReportException.ReportTargetNotFoundException::new)
                    .getMember().getMemberId());

            case BOARD_COMMENT -> Optional.of(boardCommentRepository.findById(targetId)
                    .orElseThrow(ReportException.ReportTargetNotFoundException::new)
                    .getMember().getMemberId());

            case VINTAGE_COMMENT -> {
                VintageComment comment = vintageCommentRepository.findById(targetId)
                        .orElseThrow(ReportException.ReportTargetNotFoundException::new);
                yield Optional.ofNullable(comment.getMember()).map(member -> member.getMemberId());
            }
        };
    }
}
