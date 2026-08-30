package com.vintly.interfaces.board;

public class BoardCommentException {

    public static class ParentCommentNotFoundException extends RuntimeException {
        public ParentCommentNotFoundException() {
            super("부모 댓글을 찾을 수 없습니다.");
        }
    }

    public static class ParentCommentMismatchException extends RuntimeException {
        public ParentCommentMismatchException() {
            super("부모 댓글이 다른 게시글에 속합니다.");
        }
    }

    public static class ReplyDepthExceededException extends RuntimeException {
        public ReplyDepthExceededException() {
            super("대댓글에는 댓글을 달 수 없습니다.");
        }
    }

    public static class CommentNotFoundException extends RuntimeException {
        public CommentNotFoundException() {
            super("댓글을 찾을 수 없습니다.");
        }
    }

    public static class CommentNotOwnedException extends RuntimeException {
        public CommentNotOwnedException() {
            super("본인의 댓글이 아닙니다.");
        }
    }
}
