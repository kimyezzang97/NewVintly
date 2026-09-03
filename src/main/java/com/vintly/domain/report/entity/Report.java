package com.vintly.domain.report.entity;

import com.vintly.domain.BaseEntity;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.report.ReportReason;
import com.vintly.domain.report.ReportStatus;
import com.vintly.domain.report.ReportTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity(name = "report")
@Table(name = "report", uniqueConstraints = @UniqueConstraint(
        name = "uk_report_reporter_target",
        columnNames = {"reporter_id", "target_type", "target_id"}))
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Comment("신고자 ID (탈퇴 시 orphaned, 신고 이력은 보존한다)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Member reporter;

    @Comment("신고 대상 종류")
    @Enumerated(value = EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Comment("신고 대상 ID (대상 삭제 시 orphaned)")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Comment("신고 사유")
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportReason reason;

    @Comment("상세 사유 (선택)")
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Comment("처리 상태")
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    public static Report create(Member reporter, ReportTargetType targetType, Long targetId,
                                ReportReason reason, String detail) {
        Report report = new Report();
        report.reporter = reporter;
        report.targetType = targetType;
        report.targetId = targetId;
        report.reason = reason;
        report.detail = detail;
        report.status = ReportStatus.PENDING;
        return report;
    }
}
