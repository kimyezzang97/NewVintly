package com.vintly.interfaces.auth;

public class AuthException {

    // access 토큰 만료
    public static class AccessExpiredException extends RuntimeException {
        public AccessExpiredException() {
            super("access 토큰이 만료되었습니다.");
        }
    }
}
