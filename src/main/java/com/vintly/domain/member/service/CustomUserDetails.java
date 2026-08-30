package com.vintly.domain.member.service;

import com.vintly.domain.member.Use;
import com.vintly.domain.member.entity.Member;
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

    public Long getMemberId() {
        return member.getMemberId();
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
        // 추방(X) 상태가 아니면 잠기지 않은 것으로 간주
        return member.getUseStatus() != Use.X;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 실제 자격 증명 만료 여부를 체크하는 로직이 필요할 수 있음
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 이메일 인증 완료(사용 상태 Y) 여부 체크
        return member.getUseStatus() == Use.Y;
    }
}
