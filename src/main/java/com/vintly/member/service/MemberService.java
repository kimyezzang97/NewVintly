package com.vintly.member.service;

import com.vintly.common.exception.memebr.ConflictMemberException;
import com.vintly.common.jwt.JWTUtil;
import com.vintly.common.util.mail.MailService;
import com.vintly.common.util.mail.model.MailDto;
import com.vintly.entity.Member;
import com.vintly.member.model.req.JoinReq;
import com.vintly.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class MemberService {

    @Value("${company.address}")
    private String serverAddress;

    @Value("${company.port}")
    private String serverPort;

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MailService mailService;
    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder, MailService mailService,
    JWTUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mailService = mailService;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    // email 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkEmail(String email){
        return memberRepository.existsByEmail(email);
    }

    // nickname 중복 체크
    @Transactional(readOnly = true)
    public Boolean getChkNickname(String nickname){return memberRepository.existsByNickname(nickname);}

    // 회원가입
    @Transactional(rollbackFor = Exception.class)
    public void createMember(JoinReq joinReq) {
        // 중복체크
        if(getChkEmail(joinReq.getEmail()) || getChkNickname(joinReq.getNickname())) throw new ConflictMemberException();

        // 비밀번호 암호화
        String encodePassword = bCryptPasswordEncoder.encode(joinReq.getPassword());
        joinReq.encPassword(encodePassword);

        // 회원정보 저장
        String code = memberRepository.save(joinReq.to()).getEmailCode();

        // 인증메일 발송
        mailSend(joinReq, code);
    }

    // 회원가입 인증 메일 발송
    public void mailSend(JoinReq joinReq, String code) {
        MailDto mailDTO = MailDto.builder()
                .address(joinReq.getEmail())
                .title("회원가입")
                .message("회원가입 메시지")
                .build();

        HashMap<String, Object> emailValues = new HashMap<>();
        emailValues.put("nickname", joinReq.getNickname());

        emailValues.put("url", "http://" + serverAddress + ":" + serverPort +
                "/api/v1/auth/verify?code=" + code + "&email=" + joinReq.getEmail());

        mailService.mailSend(mailDTO, emailValues,"join");
    }

    // 계정 인증
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyEmail(String code, String email){
        Optional<Member> optionalMember = memberRepository.findByEmailCodeAndEmail(code, email);

        if(optionalMember.isEmpty()){
            System.out.println("이메일 인증 실패");
            return false;
        }

        Member member = optionalMember.get();
        member.enableMember();
        memberRepository.save(member);

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
            System.out.println("==================== refresh null ====================");
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        // Refresh 토큰 만료 확인
        if (jwtUtil.isExpired(refresh)) {
            System.out.println("==================== refresh 토큰 만료 ====================");
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // Refresh 토큰인지 검증 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            System.out.println("==================== refresh 토큰인지 ====================");
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // redis 에 refresh key 저장되어 있는지 확인
        String redisKey = "refresh:"+ jwtUtil.getUsername(refresh);
        if (!redisTemplate.hasKey(redisKey)) {
            System.out.println("==================== refresh 존재여부 ====================");
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // refreshKey 는 있으나 일치하지 않으면 제거 후 400 return
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refresh)) {
            redisTemplate.delete(redisKey);
            System.out.println("==================== refresh 노일치 ====================");
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // 새로운 JWT 발급
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L); // 10분
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
