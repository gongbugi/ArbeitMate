package OpenSourceSW.ArbeitMate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS 설정
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.setAllowedOriginPatterns(List.of(
                            "http://localhost:3000",
                            "http://127.0.0.1:3000"
                    ));
                    cfg.setAllowCredentials(true);
                    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    cfg.setAllowedHeaders(List.of("*"));
                    return cfg;
                }))
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // 인가 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui.html/**", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/test/**", "/auth/**", "/notifications/**")
                        .permitAll()
                        .anyRequest().authenticated()  // 그 외 요청은 인증 필요
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 x
                )
                .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // Firebase 인증 필터 추가
                .build();
    }
}