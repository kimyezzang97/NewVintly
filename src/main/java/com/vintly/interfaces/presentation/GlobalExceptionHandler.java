package com.vintly.interfaces.presentation;

import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.vintage.VintageException;
import com.vintly.interfaces.vintageComment.VintageCommentException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 발생한 예외를 한 곳에서 관리하고 처리할 수 있게 도와주는 어노테이션
@RestControllerAdvice // 전역설정을 위한 어노테이션
public class GlobalExceptionHandler {

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

    // 닉네임 변경 - 닉네임 중복
    @ExceptionHandler(MemberException.ConflictNicknameException.class)
    protected ApiResponse<?> conflictNickname(MemberException.ConflictNicknameException e) {
        return new ApiResponse<>(false, 409, e.getMessage(), null);
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

}

