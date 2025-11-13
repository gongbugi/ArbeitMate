package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.UpdateNameRequest;
import OpenSourceSW.ArbeitMate.dto.response.MemberResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 현재 로그인한 사용자 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
        var res = memberService.getProfile(principal.memberId());
        return ResponseEntity.ok(res);
    }

    /**
     * 이름 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateMe(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid UpdateNameRequest req
    ) {
        var res = memberService.updateName(principal.memberId(), req.getName());
        return ResponseEntity.ok(res);
    }

}
