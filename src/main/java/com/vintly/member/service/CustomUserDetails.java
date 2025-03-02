package com.vintly.member.service;

import com.vintly.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    // 유저의 Role 리턴
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(member::getRole);
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // 계정이 막히지 않았다 : true
    @Override
    public boolean isAccountNonExpired() {
        // 실제 계정 만료 상태를 체크하는 로직이 필요할 수 있음
        return true;
    }

    // 계정이 잠기지 않았다 : true
    @Override
    public boolean isAccountNonLocked() {
        // 실제 계정 잠금 여부를 체크하는 로직이 필요할 수 있음
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 실제 자격 증명 만료 여부를 체크하는 로직이 필요할 수 있음
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 사용자 계정 활성화 상태 체크 (예: 사용자가 계정 비활성화 했을 경우)
        return true;
    }
}
