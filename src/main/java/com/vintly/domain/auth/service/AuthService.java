package com.vintly.domain.auth.service;

import com.vintly.domain.member.entity.Member;
import com.vintly.domain.member.repo.MemberRepository;
import com.vintly.infra.config.jwt.JWTUtil;
import com.vintly.infra.util.RefreshCookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.vintly.interfaces.presentation.ApiResponse;
import org.springframework.http.HttpHeaders;
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

        // refresh 토큰이 없으면 401 반환
        if (refresh == null) {
            log.warn("reissue 실패 - refresh token 없음, URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "refresh token null", null));
        }

        // Refresh 토큰 만료/유효성 확인 (만료 시 isExpired 호출 자체에서 ExpiredJwtException 발생)
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            log.info("refresh 토큰 만료 email : {}", e.getClaims().get("username", String.class));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "refresh token expired", null));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("reissue 실패 - 유효하지 않은 refresh token, IP: {}, reason: {}", request.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "invalid refresh token", null));
        }

        // Refresh 토큰인지 검증 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            log.warn("reissue 실패 - 유효하지 않은 토큰 category: {}, IP: {}", category, request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "invalid refresh token", null));
        }

        // redis 에 refresh key 저장되어 있는지 확인
        String redisKey = "refresh:"+ jwtUtil.getUsername(refresh);
        if (!redisTemplate.hasKey(redisKey)) {
            log.warn("reissue 실패 - Redis에 refresh token 없음, email: {}, IP: {}", jwtUtil.getUsername(refresh), request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "invalid refresh token", null));
        }

        // refreshKey 는 있으나 일치하지 않으면 제거 후 401 return
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refresh)) {
            redisTemplate.delete(redisKey);
            log.info("refreshToken not equal {}", jwtUtil.getUsername(refresh));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "invalid refresh token", null));
        }

        log.info("reissue 성공 - email: {}, IP: {}", jwtUtil.getUsername(refresh), request.getRemoteAddr());

        // 새로운 JWT 발급
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        Long memberId = jwtUtil.getMemberId(refresh);
        String newAccess = jwtUtil.createJwt("access", username, role, memberId, 1_800_000L); // 30분
        String newRefresh = jwtUtil.createJwt("refresh", username, role, memberId, 259_200_000L); // 3일

        // redis 새 Refresh 토큰 저장
        redisTemplate.opsForValue().set(redisKey, newRefresh, 259_200_000L, TimeUnit.MILLISECONDS);

        // 응답 헤더 및 쿠키 설정
        response.setHeader("access", newAccess);
        response.addHeader(HttpHeaders.SET_COOKIE, RefreshCookieUtil.create(newRefresh).toString());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // refresh 토큰 폐기 (탈퇴 시 잔여 토큰으로 재발급되는 것을 막음)
    public void deleteRefreshToken(String email) {
        redisTemplate.delete("refresh:" + email);
    }

}
