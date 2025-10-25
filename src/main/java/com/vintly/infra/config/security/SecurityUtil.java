package com.vintly.infra.config.security;

import com.vintly.domain.member.service.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class SecurityUtil {

    // 현재 로그인한 사용자 이름 가져오기
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "";  // 인증되지 않은 사용자
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            log.info("getAuthorities :: {} ", ((UserDetails) principal).getAuthorities());
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 현재 로그인한 사용자 Id 가져오기
    public static Long getCurrentMemberId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            return null;
        }
        return cud.getMemberId();
    }
}
