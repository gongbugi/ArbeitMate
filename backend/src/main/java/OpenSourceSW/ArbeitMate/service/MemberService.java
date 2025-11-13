package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.dto.request.SignupEmailRequest;
import OpenSourceSW.ArbeitMate.dto.response.MemberResponse;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord.CreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원가입 (Firebase 계정 생성 + DB 저장)
     */
    @Transactional
    public MemberResponse signupWithEmail(SignupEmailRequest req) throws FirebaseAuthException {
        // firebase user 생성
        CreateRequest create = new CreateRequest()
                .setEmail(req.getEmail())
                .setPassword(req.getPassword())
                .setDisplayName(req.getName());
        var user = FirebaseAuth.getInstance().createUser(create);

        Member m = bootstrapMemberFromFirebase(user.getUid(), req.getEmail(), req.getName());
        return toResponse(m);
    }

    /**
     * 최초 로그인 시 생성 및 연결
     */
    @Transactional
    public MemberResponse loginWithIdToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(idToken);

        String uid = decoded.getUid();
        String email = decoded.getEmail();
        String name  = decoded.getName();

        Member m = memberRepository.findByFirebaseUid(uid)
                .orElseGet(() -> bootstrapMemberFromFirebase(uid, email, name));

        return toResponse(m);
    }

    /** 필터에서 호출하는 부트스트랩 */
    @Transactional
    public Member bootstrapFromFirebase(String firebaseUid, String email, FirebaseToken decoded) {
        String name = decoded.getName();
        return bootstrapMemberFromFirebase(firebaseUid, email, name);
    }

    /** firebaseUid <-> member 연결 정책 */
    @Transactional
    public Member bootstrapMemberFromFirebase(String firebaseUid, String email, String name) {
        // 이미 연결된 계정인지 확인
        Optional<Member> byUid = memberRepository.findByFirebaseUid(firebaseUid);
        if (byUid.isPresent()) return byUid.get();

        // 같은 이메일의 기존 계정이 있다면 연결
        if (email != null) {
            Optional<Member> byEmail = memberRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                Member existing = byEmail.get();
                existing.linkFirebaseUid(firebaseUid);
                existing.updateProfile(name);
                return existing;
            }
        }

        // 신규 생성
        Member m = Member.create(email, name);
        m.linkFirebaseUid(firebaseUid);
        return memberRepository.save(m);
    }

    /**
     * 유저 프로필 불러오기
     */
    public MemberResponse getProfile(UUID memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return toResponse(m);
    }

    /**
     * 유저 이름 변경
     */
    @Transactional
    public MemberResponse updateName(UUID memberId, String newName) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        m.updateProfile(newName);
        return toResponse(m);
    }

    // 편의 메서드
    private MemberResponse toResponse(Member m) {
        return MemberResponse.builder()
                .id(m.getId().toString())
                .email(m.getEmail())
                .name(m.getName())
                .firebaseUid(m.getFirebaseUid())
                .build();
    }
}
