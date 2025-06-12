package com.vintly.interfaces.member;

public class MemberException {

    // 회원가입 - 닉네임, email 중복 확인
    public static class ConflictMemberException extends RuntimeException {
        public ConflictMemberException() {
            super("중복된 이메일 혹은 닉네임입니다.");
        }
    }

    // * 회원가입 - email 발송 실패
    public static class EmailSendException extends RuntimeException {
        public EmailSendException() {
            super("이메일 발송을 실패하였습니다.");
        }
    }

    // 닉네임 valid error
    public static class NicknameValidException extends RuntimeException {
        public NicknameValidException() {
            super("규칙을 지켜 확인해주세요.");
        }
    }
}
