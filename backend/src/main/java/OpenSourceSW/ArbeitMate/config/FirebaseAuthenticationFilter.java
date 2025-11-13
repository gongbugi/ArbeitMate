package OpenSourceSW.ArbeitMate.config;

import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.MemberService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        // 이미 인증된 경우
        if(SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", ""); // "Bearer " 이후의 토큰 값만 추출

            // Firebase Token을 검증하고 UID를 추출
            try {
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

                String firebaseUid = decoded.getUid();
                String email = decoded.getEmail();

                // firebaseUid 기반 Member 엔티티 매핑 (없을 경우 생성)
                Member member = memberRepository.findByFirebaseUid(firebaseUid)
                        .orElseGet(() -> memberService.bootstrapFromFirebase(firebaseUid, email, decoded));

                // Uid 를 이용하여 사용자 인증 객체 생성
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                var principal = new AuthPrincipal(member.getId(), firebaseUid, email);

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Spring Security의 SecurityContext에 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (FirebaseAuthException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 Firebase ID Token");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
