package com.vintly.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vintly.entity.Member;
import com.vintly.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Login 클래스
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil,
                       MemberRepository memberRepository, StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    // 로그인 정보를 Authentication Manager 에게 넘긴다. 이후 login 성공, 실패 판단
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        ObjectMapper objectMapper = new ObjectMapper();
        String username = "";
        String password = "";
        try {
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            Map<String, String> map = objectMapper.readValue(messageBody, Map.class);
            username = String.valueOf(map.get("username"));
            password = String.valueOf(map.get("password"));

        } catch (Exception e){
            e.printStackTrace();
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    // login 성공시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //유저 정보
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", username, role, 600000L); // 10분
        String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L); // 24시간

        //Refresh 토큰 저장
        addRefreshEntity(username, refresh, 86400000L); // 24시간

        //응답 설정
        response.setHeader("access", access); // header 는 access 토큰
        response.addCookie(createCookie("refresh", refresh)); // cookie 는 refresh 토큰
        response.setStatus(HttpStatus.OK.value());
    }

    private void addRefreshEntity(String username, String refresh, long expiredMs) {
        Timestamp newDate = Timestamp.from(Instant.now().plusMillis(expiredMs));

        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(""));

        String key = "refresh:" + member.getEmail(); // ex) refresh:kyc@naver.com
        redisTemplate.opsForValue().set(key, refresh, expiredMs, TimeUnit.MILLISECONDS);
    }

    // login 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);

        // 로그인 실패 시 refresh 쿠키 삭제
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0); // 쿠키 삭제
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 24시간
        cookie.setHttpOnly(true); // XSS 방어, JS 접근 차단
        cookie.setPath("/");
        //cookie.setSecure(true); // HTTPS 접근시만

        return cookie;
    }
}
