package com.vintly.infra.config.security;

import com.vintly.interfaces.filter.jwt.CustomLogoutFilter;
import com.vintly.interfaces.filter.jwt.JWTFilter;
import com.vintly.infra.config.jwt.JWTUtil;
import com.vintly.interfaces.filter.jwt.LoginFilter;
import com.vintly.domain.member.repo.MemberRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

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
                                // 인증 없이 허용
                                .requestMatchers("/login", "/logout").permitAll()
                                .requestMatchers("/api/v1/members/nickname/**", "/api/v1/members/email/**", "/api/v1/members/join").permitAll()
                                .requestMatchers("/api/v1/auth/**").permitAll()
                                .requestMatchers("/members/verify/**").permitAll()
                                //.requestMatchers("/api/v1/vintages/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                                .requestMatchers("/").permitAll()
                                // ADMIN 전용
                                .requestMatchers("/admin").hasRole("ADMIN")
                                // 나머지는 인증 필수
                                .anyRequest().authenticated()
                )

                // 인증/인가 예외 처리
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"code\":401,\"msg\":\"로그인이 필요합니다.\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"code\":403,\"msg\":\"접근 권한이 없습니다.\",\"data\":null}");
                        })
                )

                // 세션 없이 (stateless) / JWT 사용
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /** JWT 설정 **/
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)

                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, memberRepository, redisTemplate), UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(new CustomLogoutFilter(jwtUtil, redisTemplate), LogoutFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())); // 최신 방식으로 CORS 설정
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

    @Bean
    @ConditionalOnProperty(name = "spring.h2.console.enabled",havingValue = "true")
    public WebSecurityCustomizer configureH2ConsoleEnable() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console());
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*")); // 전체 URL 허용
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setExposedHeaders(Arrays.asList("access", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowCredentials(true); // 쿠키 인증 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
