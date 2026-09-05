package com.vintly.interfaces.vintage;

public class VintageException {

    // 빈티지 가게
    public static class VintageCreateException extends RuntimeException {
        public VintageCreateException() {
            super("빈티지 매장 등록에 실패하였습니다.");
        }

        // 원인을 물려줘야 상위 스택트레이스만 보고도 실제 실패 사유를 알 수 있다
        public VintageCreateException(Throwable cause) {
            super("빈티지 매장 등록에 실패하였습니다.", cause);
        }
    }

}
