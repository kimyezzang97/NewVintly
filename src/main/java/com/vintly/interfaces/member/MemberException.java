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

    // 회원 정보를 찾을 수 없음
    public static class MemberNotFoundException extends RuntimeException {
        public MemberNotFoundException() {
            super("회원 정보를 찾을 수 없습니다.");
        }
    }

    // 현재 비밀번호 불일치
    public static class PasswordNotMatchException extends RuntimeException {
        public PasswordNotMatchException() {
            super("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    // 닉네임 변경 - 닉네임 중복
    public static class ConflictNicknameException extends RuntimeException {
        public ConflictNicknameException() {
            super("이미 사용 중인 닉네임입니다.");
        }
    }

    // 닉네임 변경 - 14일 제한
    public static class NicknameChangeTooSoonException extends RuntimeException {
        private final long remainingDays;

        public NicknameChangeTooSoonException(long remainingDays) {
            super("닉네임은 14일에 한 번만 변경할 수 있습니다. 변경 가능까지 " + remainingDays + "일 남았습니다.");
            this.remainingDays = remainingDays;
        }

        public long getRemainingDays() {
            return remainingDays;
        }
    }
}
