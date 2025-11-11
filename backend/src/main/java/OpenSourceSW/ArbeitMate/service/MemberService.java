// src/main/java/OpenSourceSW/ArbeitMate/service/MemberService.java
package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.dto.MemberLoginRequest;
import OpenSourceSW.ArbeitMate.dto.MemberSignUpRequest;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // [2. PasswordEncoder 주입]

    @Transactional
    public UUID signUp(MemberSignUpRequest request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(request.getPassword()); // (수정된 코드)

        Member newMember = Member.create(
                request.getEmail(),
                encryptedPassword,
                request.getName(),
                request.getPhone()
        );

        Member savedMember = memberRepository.save(newMember);
        return savedMember.getId();
    }
    @Transactional(readOnly = true)
    public UUID login(MemberLoginRequest request) {

        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 비교
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }



        // 3. 로그인 성공 시 회원 ID(UUID) 반환
        return member.getId();
    }
   
}