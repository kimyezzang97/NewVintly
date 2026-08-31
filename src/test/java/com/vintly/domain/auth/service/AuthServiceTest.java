package com.vintly.domain.auth.service;

import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.infra.config.jwt.JWTUtil;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-unit-tests-must-be-32-bytes-or-more";
    private static final String EMAIL = "test@example.com";
    private static final String ROLE = "ROLE_USER";
    private static final Long MEMBER_ID = 1L;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private JWTUtil jwtUtil;
    private AuthService authService;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil(SECRET);
        authService = new AuthService(jwtUtil, redisTemplate, memberRepository);
        secretKey = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Test
    @DisplayName("재발급 시 로그인과 동일하게 access는 30분, refresh는 3일짜리로 발급되고 쿠키 Max-Age도 3일이다.")
    void reissueUsesSameTokenLifetimeAsLogin() {
        // Arrange
        String refresh = jwtUtil.createJwt("refresh", EMAIL, ROLE, MEMBER_ID, 259_200_000L);
        String redisKey = "refresh:" + EMAIL;

        when(redisTemplate.hasKey(redisKey)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(refresh);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", refresh));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        authService.reissue(request, response);

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);

        String newAccess = response.getHeader("access");
        assertThat(newAccess).isNotNull();
        assertThat(jwtUtil.getCategory(newAccess)).isEqualTo("access");
        // JWT의 exp/iat는 초 단위로 내림 처리되므로, 발급 시각(iat) 대비 만료 시각(exp)의 차이로 수명을 검증한다.
        assertThat(lifetime(newAccess)).isEqualTo(1_800_000L);

        Cookie refreshCookie = findCookie(response, "refresh");
        assertThat(refreshCookie).isNotNull();
        String newRefresh = refreshCookie.getValue();
        assertThat(jwtUtil.getCategory(newRefresh)).isEqualTo("refresh");
        assertThat(lifetime(newRefresh)).isEqualTo(259_200_000L);
        assertThat(refreshCookie.getMaxAge()).isEqualTo(3 * 24 * 60 * 60);

        verify(valueOperations).set(eq(redisKey), anyString(), eq(259_200_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("refresh 쿠키가 없으면 401을 반환한다.")
    void reissueFailsWhenNoRefreshCookie() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        var result = authService.reissue(request, response);

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("만료된 refresh 토큰이면 401을 반환한다.")
    void reissueFailsWhenRefreshTokenExpired() {
        // Arrange
        String expiredRefresh = jwtUtil.createJwt("refresh", EMAIL, ROLE, MEMBER_ID, -1_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", expiredRefresh));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        var result = authService.reissue(request, response);

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("access 토큰이 refresh 쿠키에 담겨오면(category 불일치) 401을 반환한다.")
    void reissueFailsWhenCategoryIsNotRefresh() {
        // Arrange
        String accessAsRefresh = jwtUtil.createJwt("access", EMAIL, ROLE, MEMBER_ID, 259_200_000L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", accessAsRefresh));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        var result = authService.reissue(request, response);

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @DisplayName("Redis에 저장된 refresh 토큰과 값이 다르면 401을 반환하고 Redis 키를 삭제한다.")
    void reissueFailsAndDeletesKeyWhenStoredTokenMismatches() {
        // Arrange
        String refresh = jwtUtil.createJwt("refresh", EMAIL, ROLE, MEMBER_ID, 259_200_000L);
        String redisKey = "refresh:" + EMAIL;

        when(redisTemplate.hasKey(redisKey)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn("different-token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh", refresh));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        var result = authService.reissue(request, response);

        // Assert
        assertThat(result.getStatusCode().value()).isEqualTo(401);
        verify(redisTemplate).delete(redisKey);
    }

    private long lifetime(String token) {
        var claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
    }

    private Cookie findCookie(MockHttpServletResponse response, String name) {
        for (Cookie cookie : response.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}
