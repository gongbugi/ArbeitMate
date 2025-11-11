package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.MemberLoginRequest;
import OpenSourceSW.ArbeitMate.dto.MemberSignUpRequest;
import OpenSourceSW.ArbeitMate.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody MemberSignUpRequest request) {

        UUID memberId = memberService.signUp(request);

        return ResponseEntity.ok("회원가입 성공. 회원 ID: " + memberId);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody MemberLoginRequest request) {

        UUID memberId = memberService.login(request);

        return ResponseEntity.ok("로그인 성공. 회원 ID: " + memberId);
    }
}