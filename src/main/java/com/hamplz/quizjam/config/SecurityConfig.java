package com.hamplz.quizjam.config;

import com.hamplz.quizjam.auth.service.AuthService;
import com.hamplz.quizjam.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception {
        // 커스텀 필터 생성
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, authService);

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // H2는 CSRF 무시
            )
            // ✅ CSRF 및 CORS 설정
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ✅ H2 콘솔 접근 허용 & 프레임 허용
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // H2 콘솔 허용
            //.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**", "/api/kakao/login", "/api/kakao/callback").permitAll()  // ✅ 콘솔 접근 허용
                .requestMatchers("/api/auth/refresh").permitAll()
                .anyRequest().permitAll()                      // 나머지는 전부 허용 (개발 단계)
                //.anyRequest().authenticated()
            )
            // UsernamePasswordAuthenticationFilter 앞에서 JWT 인증 시도
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CORS 설정을 Bean으로 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트엔드 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // ✅ 쿠키 포함 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}