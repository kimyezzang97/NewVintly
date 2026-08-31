package com.vintly.interfaces.filter.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.domain.member.service.CustomUserDetails;
import com.vintly.infra.config.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("로그인 성공 시 access는 30분, refresh는 3일짜리로 발급되고 refresh 쿠키의 Max-Age도 3일로 맞춰진다.")
    void successfulAuthenticationIssuesTokensAndCookieWithMatchingExpiry() throws Exception {
        // Arrange
        String email = "test@example.com";
        Member member = new Member(1L, email, "encoded", "nickname",
                null, "ROLE_USER", Use.Y, null, null);
        CustomUserDetails principal = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        when(jwtUtil.createJwt("access", email, "ROLE_USER", 1L, 1_800_000L)).thenReturn("access-token");
        when(jwtUtil.createJwt("refresh", email, "ROLE_USER", 1L, 259_200_000L)).thenReturn("refresh-token");
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil, memberRepository, redisTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        loginFilter.successfulAuthentication(request, response, null, authentication);

        // Assert
        verify(valueOperations).set("refresh:" + email, "refresh-token", 259_200_000L, TimeUnit.MILLISECONDS);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("access")).isEqualTo("access-token");

        Cookie refreshCookie = findCookie(response, "refresh");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("refresh-token");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(3 * 24 * 60 * 60);
        assertThat(refreshCookie.isHttpOnly()).isTrue();

        JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
        assertThat(body.get("memberId").asLong()).isEqualTo(1L);
        assertThat(body.get("nickname").asText()).isEqualTo("nickname");
        assertThat(body.get("role").asText()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("로그인 실패 시 401을 반환하고 refresh 쿠키를 삭제한다.")
    void unsuccessfulAuthenticationReturns401AndClearsRefreshCookie() {
        // Arrange
        LoginFilter loginFilter = new LoginFilter(authenticationManager, jwtUtil, memberRepository, redisTemplate);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        loginFilter.unsuccessfulAuthentication(request, response, new BadCredentialsException("bad credentials"));

        // Assert
        assertThat(response.getStatus()).isEqualTo(401);
        Cookie refreshCookie = findCookie(response, "refresh");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getMaxAge()).isEqualTo(0);
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
