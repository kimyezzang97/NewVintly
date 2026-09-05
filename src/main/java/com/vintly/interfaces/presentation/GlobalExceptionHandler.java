package com.vintly.interfaces.presentation;

import com.vintly.interfaces.block.MemberBlockException;
import com.vintly.interfaces.board.BoardException;
import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.report.ReportException;
import com.vintly.interfaces.vintage.VintageException;
import com.vintly.interfaces.vintagecomment.VintageCommentException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

// 발생한 예외를 한 곳에서 관리하고 처리할 수 있게 도와주는 어노테이션
@RestControllerAdvice // 전역설정을 위한 어노테이션
public class GlobalExceptionHandler {

    // 안내 문구가 실제 설정과 어긋나지 않도록 yml 값을 그대로 읽어 쓴다
    private final String maxFileSize;
    private final String maxRequestSize;

    public GlobalExceptionHandler(
            @Value("${spring.servlet.multipart.max-file-size}") String maxFileSize,
            @Value("${spring.servlet.multipart.max-request-size}") String maxRequestSize) {
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
    }

    /**
     * 업로드 용량 초과 (spring.servlet.multipart 한도).
     *
     * <p>멀티파트 파싱 단계에서 터지므로 컨트롤러/파사드의 try-catch에는 잡히지 않는다.
     * 참고로 Nginx의 client_max_body_size를 먼저 넘기면 요청이 여기까지 오지도 못하고
     * Nginx가 실제 HTTP 413(HTML)을 반환한다 - 프론트는 두 형태를 모두 처리해야 한다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ApiResponse<?> maxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        String msg = String.format("이미지 용량이 너무 큽니다. 장당 %s, 전체 %s 이하로 올려주세요.",
                maxFileSize, maxRequestSize);
        return new ApiResponse<>(false, 413, msg, null);
    }

    // @Valid 또는 @Validated로 binding error 발생시 발생하는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> processValidationError(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();

        String errorMsg = bindingResult.getFieldErrors().get(0).getDefaultMessage();
        return new ApiResponse<>(false, 400, errorMsg, null);
    }

    // PathVariable valid exception
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> pathValidationError(ConstraintViolationException exception) {
        return new ApiResponse<>(false, 400, "규칙을 지켜 확인해주세요.", null);
    }

    // 존재하지 않는 리소스 조회/수정/삭제 시도 (ex. 없는 vintageId로 상세조회/수정/삭제)
    @ExceptionHandler(EntityNotFoundException.class)
    protected ApiResponse<?> entityNotFound(EntityNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    // 도메인 엔티티의 잘못된 인자 검증 실패 (ex. parentCommentId <= 0, 대표 이미지 ID null)
    @ExceptionHandler(IllegalArgumentException.class)
    protected ApiResponse<?> illegalArgument(IllegalArgumentException e) {
        return new ApiResponse<>(false, 400, e.getMessage(), null);
    }

    /**
     * [member]
     */
    // 닉네임 규칙 valid
    @ExceptionHandler(MemberException.NicknameValidException.class)
    protected ApiResponse<?> nicknameValidError(MemberException.NicknameValidException e) {
        return new ApiResponse<>(false, 400, e.getMessage(), null);
    }

    // 중복확인 필요
    @ExceptionHandler(MemberException.ConflictMemberException.class)
    protected ApiResponse<?> conflictMember(MemberException.ConflictMemberException e) {
        return new ApiResponse<>(false, 409, e.getMessage(), null);
    }

    // 회원가입 - 이메일 인증 대기 중
    @ExceptionHandler(MemberException.PendingEmailVerificationException.class)
    protected ApiResponse<?> pendingEmailVerification(MemberException.PendingEmailVerificationException e) {
        return new ApiResponse<>(false, 409, e.getMessage(), null);
    }

    // 닉네임 변경 - 닉네임 중복
    @ExceptionHandler(MemberException.ConflictNicknameException.class)
    protected ApiResponse<?> conflictNickname(MemberException.ConflictNicknameException e) {
        return new ApiResponse<>(false, 409, e.getMessage(), null);
    }

    // 닉네임 변경 - 14일 제한
    @ExceptionHandler(MemberException.NicknameChangeTooSoonException.class)
    protected ApiResponse<?> nicknameChangeTooSoon(MemberException.NicknameChangeTooSoonException e) {
        return new ApiResponse<>(false, 429, e.getMessage(), null);
    }

    // 현재 비밀번호 불일치
    @ExceptionHandler(MemberException.PasswordNotMatchException.class)
    protected ApiResponse<?> passwordNotMatch(MemberException.PasswordNotMatchException e) {
        return new ApiResponse<>(false, 400, e.getMessage(), null);
    }

    // 회원 정보를 찾을 수 없음
    @ExceptionHandler(MemberException.MemberNotFoundException.class)
    protected ApiResponse<?> memberNotFound(MemberException.MemberNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    // 회원가입 - 이메일 발송 실패
    @ExceptionHandler(MemberException.EmailSendException.class)
    protected ApiResponse<?> emailSendError(MemberException.EmailSendException e) {
        return new ApiResponse<>(false, 500, e.getMessage(), null);
    }

    /**
     * [vintage]
     */

    @ExceptionHandler(VintageException.VintageCreateException.class)
    protected ApiResponse<?> emailSendError(VintageException.VintageCreateException e) {
        return new ApiResponse<>(false, 500, e.getMessage(), null);
    }

    /**
     * [board]
     */

    @ExceptionHandler(BoardException.BoardNotFoundException.class)
    protected ApiResponse<?> boardNotFound(BoardException.BoardNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    @ExceptionHandler(BoardException.BoardForbiddenException.class)
    protected ApiResponse<?> boardForbidden(BoardException.BoardForbiddenException e) {
        return new ApiResponse<>(false, 403, e.getMessage(), null);
    }

    @ExceptionHandler(BoardException.BoardAlreadyDeletedException.class)
    protected ApiResponse<?> boardAlreadyDeleted(BoardException.BoardAlreadyDeletedException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    /**
     * [vintageComment]
     */

    // 부모 댓글이 존재하지 않음
    @ExceptionHandler(VintageCommentException.ParentCommentNotFoundException.class)
    protected ApiResponse<?> parentCommentNotFound(VintageCommentException.ParentCommentNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    // 부모 댓글이 다른 매장에 속해 있음
    @ExceptionHandler(VintageCommentException.ParentCommentMismatchException.class)
    protected ApiResponse<?> parentCommentMismatch(VintageCommentException.ParentCommentMismatchException e) {
        return new ApiResponse<>(false, 400, e.getMessage(), null);
    }

    // 대댓글에 대댓글 불가
    @ExceptionHandler(VintageCommentException.ReplyDepthExceededException.class)
    protected ApiResponse<?> replyDepthExceeded(VintageCommentException.ReplyDepthExceededException e) {
        return new ApiResponse<>(false, 400, e.getMessage(), null);
    }

    // 댓글이 존재하지 않음
    @ExceptionHandler(VintageCommentException.CommentNotFoundException.class)
    protected ApiResponse<?> commentNotFound(VintageCommentException.CommentNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    // 본인의 댓글이 아님
    @ExceptionHandler(VintageCommentException.CommentNotOwnedException.class)
    protected ApiResponse<?> commentNotOwned(VintageCommentException.CommentNotOwnedException e) {
        return new ApiResponse<>(false, 403, e.getMessage(), null);
    }

    /**
     * [report]
     */

    // 같은 대상을 두 번 신고 (선체크 또는 유니크 제약)
    @ExceptionHandler(ReportException.DuplicateReportException.class)
    protected ApiResponse<?> duplicateReport(ReportException.DuplicateReportException e) {
        return new ApiResponse<>(false, 409, e.getMessage(), null);
    }

    // 신고 대상이 존재하지 않음
    @ExceptionHandler(ReportException.ReportTargetNotFoundException.class)
    protected ApiResponse<?> reportTargetNotFound(ReportException.ReportTargetNotFoundException e) {
        return new ApiResponse<>(false, 404, e.getMessage(), null);
    }

    // 본인이 작성한 콘텐츠 신고
    @ExceptionHandler(ReportException.SelfReportException.class)
    protected ApiResponse<?> selfReport(ReportException.SelfReportException e) {
        return new ApiResponse<>(false, 403, e.getMessage(), null);
    }

    /**
     * [block]
     */

    // 자기 자신 차단
    @ExceptionHandler(MemberBlockException.SelfBlockException.class)
    protected ApiResponse<?> selfBlock(MemberBlockException.SelfBlockException e) {
        return new ApiResponse<>(false, 403, e.getMessage(), null);
    }

    // 나를 차단한 회원의 글에 댓글 작성 시도
    @ExceptionHandler(MemberBlockException.BlockedByAuthorException.class)
    protected ApiResponse<?> blockedByAuthor(MemberBlockException.BlockedByAuthorException e) {
        return new ApiResponse<>(false, 403, e.getMessage(), null);
    }

}

