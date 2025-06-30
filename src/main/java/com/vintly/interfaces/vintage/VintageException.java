package com.vintly.interfaces.vintage;

public class VintageException {

    // 빈티지 가게
    public static class VintageCreateException extends RuntimeException {
        public VintageCreateException() {
            super("빈티지 매장 등록에 실패하였습니다.");
        }
    }

}
