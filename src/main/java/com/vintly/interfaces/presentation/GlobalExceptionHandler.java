package com.vintly.interfaces.presentation;

import com.vintly.interfaces.member.MemberException;
import com.vintly.interfaces.auth.AccessExpiredException;
import com.vintly.interfaces.vintage.VintageException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
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

    // 회원가입 - 이메일 발송 실패
    @ExceptionHandler(MemberException.EmailSendException.class)
    protected ApiResponse<?> emailSendError(MemberException.EmailSendException e) {
        return new ApiResponse<>(false, 500, e.getMessage(), null);
    }

    // 로그인 - access 토큰 만료
    @ExceptionHandler(AccessExpiredException.class)
    protected ResponseEntity<?> AccessExpired(AccessExpiredException exception) {
        return ResponseEntity.status(401).build();
    }

    /**
     * [vintage]
     */

    @ExceptionHandler(VintageException.VintageCreateException.class)
    protected ApiResponse<?> emailSendError(VintageException.VintageCreateException e) {
        return new ApiResponse<>(false, 500, e.getMessage(), null);
    }

}

