package com.vintly.interfaces.vintagecomment;

public class VintageCommentException {

    // 부모 댓글이 존재하지 않음
    public static class ParentCommentNotFoundException extends RuntimeException {
        public ParentCommentNotFoundException() {
            super("부모 댓글이 존재하지 않습니다.");
        }
    }

    // 부모 댓글이 다른 매장에 속해 있음
    public static class ParentCommentMismatchException extends RuntimeException {
        public ParentCommentMismatchException() {
            super("부모 댓글이 다른 매장에 속해 있습니다.");
        }
    }

    // 대댓글에 대댓글 불가
    public static class ReplyDepthExceededException extends RuntimeException {
        public ReplyDepthExceededException() {
            super("대댓글에는 다시 대댓글을 달 수 없습니다.");
        }
    }

    // 댓글이 존재하지 않음
    public static class CommentNotFoundException extends RuntimeException {
        public CommentNotFoundException() {
            super("댓글이 존재하지 않습니다.");
        }
    }

    // 본인의 댓글이 아님
    public static class CommentNotOwnedException extends RuntimeException {
        public CommentNotOwnedException() {
            super("본인의 댓글만 삭제할 수 있습니다.");
        }
    }
}
