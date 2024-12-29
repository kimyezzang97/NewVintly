package com.vintly.common.exception.memebr;

import lombok.Getter;

@Getter
public class ConflictMemberException extends RuntimeException {

    // nickname or email 중복
    public ConflictMemberException(){};
}
