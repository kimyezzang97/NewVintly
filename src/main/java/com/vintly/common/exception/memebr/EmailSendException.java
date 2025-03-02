package com.vintly.common.exception.memebr;

import com.vintly.common.exception.StatusEnum;
import lombok.Getter;

/**
 * 회원가입 - email 발송 실패
 */
@Getter
public class EmailSendException extends RuntimeException {
    private final StatusEnum status;
    // nickname or email 중복
    public EmailSendException(){
        this.status = StatusEnum.EMAIL_SEND_ERROR;
    };
}
