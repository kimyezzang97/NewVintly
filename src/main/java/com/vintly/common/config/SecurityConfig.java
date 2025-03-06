package com.vintly.common.config;

import com.vintly.common.jwt.CustomLogoutFilter;
import com.vintly.common.jwt.JWTFilter;
import com.vintly.common.jwt.JWTUtil;
import com.vintly.common.jwt.LoginFilter;
import com.vintly.member.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration // Configuration 등록하기
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록이 된다.
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil,
                          MemberRepository memberRepository, StringRedisTemplate redisTemplate) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                // csrf : 주로 SSR인 경우 필요(html 코드를 수정하기 때문이라고 함), API 이기에 disable-
                .csrf(AbstractHttpConfigurer::disable)

                // form 로그인 방식 미사용으로 disable
                .formLogin(AbstractHttpConfigurer::disable)

                // http 인증방식 미사용으로 disable
                .httpBasic(AbstractHttpConfigurer::disable)

                // 경로 인증 인가 설정
                .authorizeHttpRequests(
                        (auth) -> auth
                        .requestMatchers("/login", "/logout", "/api/v1/members/**", "/api/v1/auth/**",
                                    "/members/verify/**").permitAll() // 모든 경로 허용
                        .requestMatchers("/admin").hasRole("ADMIN") // admin 권한자만 사용
                        .anyRequest().authenticated()
                ) // 로그인한 사용자는 가능

                // 세션 없이 (stateless) / JWT 사용
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /** JWT 설정 **/
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)

                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, memberRepository, redisTemplate), UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(new CustomLogoutFilter(jwtUtil, redisTemplate), LogoutFilter.class);
        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 반환 Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
}



// 이전 사용 cors 설정
// cors 기본 값은 모든 origin, 헤더, HTTP 메서드(GET, POST..), 자격 증명 허용, Preflight 요청의 최대 수명을 설정하지 않음
// .cors(Customizer.withDefaults())