package com.vintly.infra.util;

import com.vintly.domain.member.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 존재하지 않습니다.");
        }

        return((CustomUserDetails) authentication.getPrincipal()).getUsername();
    }
}
