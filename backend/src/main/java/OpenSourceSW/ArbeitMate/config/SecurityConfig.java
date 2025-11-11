package OpenSourceSW.ArbeitMate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티를 활성화합니다.
public class SecurityConfig {

    // 1. 비밀번호 암호화 도구(PasswordEncoder)를 Bean으로 등록합니다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 방식 사용
    }

    // 2. API 보안 설정을 구성합니다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // (1) CSRF 보호 비활성화 (API 서버는 보통 비활성화합니다)
                .csrf(csrf -> csrf.disable())

                // (2) 세션을 사용하지 않음 (JWT 같은 토큰 방식에 필요)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // (3) API 경로별 접근 권한 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/members/signup", "/members/login").permitAll()

                        // 그 외의 모든 요청은 일단 막음 (나중에 인증 필요하게 수정)
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}