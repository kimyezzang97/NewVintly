package com.vintly.interfaces.filter.jwt;

import com.vintly.infra.config.jwt.JWTUtil;
import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.service.CustomUserDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 access 키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("access");

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null){
            filterChain.doFilter(request, response);
            return ;
        }

        // 토큰 만료 여부 확인, 만료시 401
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("access token expired - URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
            sendUnauthorized(response, "access token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("invalid access token - URI: {}, IP: {}, reason: {}", request.getRequestURI(), request.getRemoteAddr(), e.getMessage());
            sendUnauthorized(response, "invalid access token");
            return;
        }

        // 토큰이 access 인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
            log.warn("invalid access token (category: {}) - URI: {}, IP: {}", category, request.getRequestURI(), request.getRemoteAddr());
            sendUnauthorized(response, "invalid access token");
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);
        Long memberId   = jwtUtil.getMemberId(accessToken);  // ← claim에서 꺼내기

        // email, role 만 생성
        Member member = new Member(memberId, username, null, null,
                null, role, null, null, null);

        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"code\":401,\"msg\":\"" + message + "\",\"data\":null}");
    }
}
