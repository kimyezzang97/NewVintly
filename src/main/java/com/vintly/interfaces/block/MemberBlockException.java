package com.vintly.interfaces.block;

public class MemberBlockException {

    // 자기 자신 차단
    public static class SelfBlockException extends RuntimeException {
        public SelfBlockException() {
            super("자기 자신은 차단할 수 없습니다.");
        }
    }

    // 나를 차단한 회원의 글에 댓글 작성 시도
    public static class BlockedByAuthorException extends RuntimeException {
        public BlockedByAuthorException() {
            super("댓글을 작성할 수 없습니다.");
        }
    }
}
