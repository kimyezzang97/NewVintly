package com.vintly.common.exception;

import com.vintly.common.config.ApiResponse;
import com.vintly.common.exception.memebr.AccessExpiredException;
import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.common.exception.memebr.EmailSendException;
import com.vintly.common.exception.memebr.NicknameValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 발생한 예외를 한 곳에서 관리하고 처리할 수 있게 도와주는 어노테이션
@RestControllerAdvice // 전역설정을 위한 어노테이션
public class ExceptionAdvisor {

    // @Valid 또는 @Validated로 binding error 발생시 발생하는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> processValidationError(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();

        String errorMsg = bindingResult.getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.BAD_REQUEST)
                .msg(errorMsg)
                .data("")
                .build());
    }

    // PathVariable valid exception
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> pathValidationError(ConstraintViolationException exception) {
        String errorMsg = "규칙을 지켜 확인해주세요.";

        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.VALID_ERROR)
                .msg(errorMsg)
                .data("")
                .build());
    }

    /**
     * [member]
     */
    // 닉네임, email 규칙 valid
    @ExceptionHandler(NicknameValidException.class)
    protected ResponseEntity<?> nicknameValidError(NicknameValidException exception) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(exception.getStatus())
                .msg(exception.getStatus().getMessage())
                .data("")
                .build());
    }

    // 중복확인 필요
    @ExceptionHandler(ConflictMemberException.class)
    protected ResponseEntity<?> conflictMember(ConflictMemberException exception) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.JOIN_CONFLICT)
                .msg(exception.getStatus().getMessage())
                .data("")
                .build());
    }

    // 회원가입 - 이메일 발송 실패
    @ExceptionHandler(EmailSendException.class)
    protected ResponseEntity<?> emailSendError(EmailSendException exception) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(StatusEnum.EMAIL_SEND_ERROR)
                .msg(exception.getStatus().getMessage())
                .data("")
                .build());
    }

    // 로그인 - access 토큰 만료
    @ExceptionHandler(AccessExpiredException.class)
    protected ResponseEntity<?> AccessExpired(AccessExpiredException exception) {
        return ResponseEntity.status(401).build();
    }
}

