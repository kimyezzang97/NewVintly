package com.vintly.infra.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * refresh 토큰 쿠키를 만드는 유일한 통로.
 *
 * <p>admin 프론트와 백엔드가 서로 다른 사이트(cross-site)에서 동작하므로 SameSite=None이 필요하다.
 * 값을 명시하지 않으면 브라우저가 Lax로 취급해, 프론트의 XHR(재발급 등)에 쿠키가 실리지 않는다.
 * SameSite=None은 Secure를 요구하므로 둘은 항상 함께 간다.
 *
 * <p>발급/삭제/로그아웃이 네 곳에 흩어져 있어 속성이 서로 어긋났던 이력이 있다.
 * 브라우저는 name/path/domain이 같아도 속성이 다르면 삭제 쿠키를 원본과 매칭하지 못하므로,
 * 반드시 이 클래스를 거쳐 생성할 것.
 */
public final class RefreshCookieUtil {

    public static final String COOKIE_NAME = "refresh";

    // refresh 토큰 만료시간(3일)과 일치시킨다
    private static final Duration MAX_AGE = Duration.ofDays(3);

    private RefreshCookieUtil() {
    }

    // 발급용
    public static ResponseCookie create(String refreshToken) {
        return builder(refreshToken).maxAge(MAX_AGE).build();
    }

    // 삭제용 (로그아웃, 로그인 실패)
    public static ResponseCookie expire() {
        return builder("").maxAge(0).build();
    }

    private static ResponseCookie.ResponseCookieBuilder builder(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)   // XSS 방어, JS 접근 차단
                .secure(true)     // SameSite=None의 전제 조건
                .sameSite("None") // 크로스사이트 XHR에도 쿠키를 실어 보낸다
                .path("/");
    }
}
