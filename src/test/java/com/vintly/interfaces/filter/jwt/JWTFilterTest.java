package com.vintly.interfaces.filter.jwt;

import com.vintly.infra.config.jwt.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("access 헤더가 없으면 인증 없이 다음 필터로 넘어간다.")
    void shouldPassThroughWhenNoAccessHeader() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("만료된 access 토큰이면 401과 만료 메시지를 반환하고 필터체인을 진행하지 않는다.")
    void shouldReturn401WhenAccessTokenExpired() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("access", "expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        when(jwtUtil.isExpired("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "token expired"));

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("access token expired");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("변조되거나 형식이 깨진 access 토큰이면 500이 아니라 401을 반환한다.")
    void shouldReturn401InsteadOf500WhenAccessTokenIsMalformed() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("access", "malformed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        when(jwtUtil.isExpired("malformed-token")).thenThrow(new MalformedJwtException("broken token"));

        // Act & Assert : 예외가 필터 밖으로 전파되지 않아야 한다 (전파되면 500으로 이어짐)
        jwtFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("invalid access token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("category가 access가 아니면 401을 반환한다.")
    void shouldReturn401WhenCategoryIsNotAccess() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("access", "refresh-token-in-access-header");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("refresh");

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("invalid access token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("유효한 access 토큰이면 SecurityContext에 인증 정보를 설정하고 다음 필터로 진행한다.")
    void shouldAuthenticateWhenAccessTokenValid() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("access", "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JWTFilter jwtFilter = new JWTFilter(jwtUtil);

        when(jwtUtil.isExpired("valid-token")).thenReturn(false);
        when(jwtUtil.getCategory("valid-token")).thenReturn("access");
        when(jwtUtil.getUsername("valid-token")).thenReturn("test@example.com");
        when(jwtUtil.getRole("valid-token")).thenReturn("ROLE_USER");
        when(jwtUtil.getMemberId("valid-token")).thenReturn(1L);

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("test@example.com");
    }
}
