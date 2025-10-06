package com.vintly.domain.auth.service;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.infra.config.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthService {

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    public AuthService(JWTUtil jwtUtil, StringRedisTemplate redisTemplate, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    // 계정 인증
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(String code, String email){
        Optional<Member> optionalMember = memberRepository.findByEmailCodeAndEmail(code, email);

        if(optionalMember.isEmpty()){
            log.info("이메일 인증 실패 {}", email);
            return false;
        }

        Member member = optionalMember.get();
        member.enableMember();

        return true;
    }

    // refresh 토큰으로 재발급
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        // refresh 토큰이 없으면 400 반환
        if (refresh == null) {
            //System.out.println("==================== refresh null ====================");
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // Refresh 토큰 만료 확인
        if (jwtUtil.isExpired(refresh)) {
            log.info("refresh 토큰 만료 email : {}",jwtUtil.getUsername(refresh));
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // Refresh 토큰인지 검증 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // redis 에 refresh key 저장되어 있는지 확인
        String redisKey = "refresh:"+ jwtUtil.getUsername(refresh);
        if (!redisTemplate.hasKey(redisKey)) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // refreshKey 는 있으나 일치하지 않으면 제거 후 400 return
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refresh)) {
            redisTemplate.delete(redisKey);
            log.info("refreshToken not equal {}", jwtUtil.getUsername(refresh));
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 새로운 JWT 발급
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String newAccess = jwtUtil.createJwt("access", username, role, 86400000L); // 10분 600000L
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L); // 24시간

        // redis 새 Refresh 토큰 저장
        redisTemplate.opsForValue().set(redisKey, newRefresh, 86400000L, TimeUnit.MILLISECONDS);

        // 응답 헤더 및 쿠키 설정
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 24시간
        cookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능하도록
        cookie.setHttpOnly(true);
        //cookie.setSecure(true); HTTPS 에서만 전송

        return cookie;
    }
}
