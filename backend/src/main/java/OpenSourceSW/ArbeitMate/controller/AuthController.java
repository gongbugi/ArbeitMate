package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.LoginWithIdTokenRequest;
import OpenSourceSW.ArbeitMate.dto.request.SignupEmailRequest;
import OpenSourceSW.ArbeitMate.dto.response.MemberResponse;
import OpenSourceSW.ArbeitMate.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    /**
     * 회원가입 (이메일, 비밀번호, 이름)
     */
    @PostMapping("/signup-email")
    public ResponseEntity<MemberResponse> signupEmail(@RequestBody @Valid SignupEmailRequest req) throws Exception{
        var res = memberService.signupWithEmail(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 로그인: Firebase ID 토큰을 받아서 서버가 검증 -> db 저장
     */
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody @Valid LoginWithIdTokenRequest req) throws Exception {
        var res = memberService.loginWithIdToken(req.getIdToken());
        return ResponseEntity.ok(res);
    }

}
