package com.vintly.interfaces.board;

public class BoardException {

    public static class BoardNotFoundException extends RuntimeException {
        public BoardNotFoundException() {
            super("게시글을 찾을 수 없습니다.");
        }
    }

    public static class BoardForbiddenException extends RuntimeException {
        public BoardForbiddenException() {
            super("게시글에 대한 권한이 없습니다.");
        }
    }

    public static class BoardAlreadyDeletedException extends RuntimeException {
        public BoardAlreadyDeletedException() {
            super("이미 삭제된 게시글입니다.");
        }
    }
}
