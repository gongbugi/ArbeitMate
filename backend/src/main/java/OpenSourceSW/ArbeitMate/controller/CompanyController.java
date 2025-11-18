package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.UpdateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CreateCompanyResponse;
import OpenSourceSW.ArbeitMate.dto.response.ParticipateCompanyResponse;
import OpenSourceSW.ArbeitMate.dto.response.UpdateCompanyResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 회사 생성
     */
    @PostMapping("/create")
    public ResponseEntity<CreateCompanyResponse> createCompany(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CreateCompanyRequest req) {
        var res = companyService.createCompany(principal.memberId(), req);
        return ResponseEntity.ok(res);
    }

    /**
     * 방(회사) 참가
     */
    @PostMapping("/participate")
    public ResponseEntity<ParticipateCompanyResponse> participateCompany(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody ParticipateCompanyRequest req) {
        var res = companyService.participateCompany(principal.memberId(), req);
        return ResponseEntity.ok(res);
    }

    /**
     * 회사 정보 수정
     */
    @PatchMapping("/{companyId}")
    public ResponseEntity<UpdateCompanyResponse> updateCompany(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @RequestBody UpdateCompanyRequest req) {

        var res = companyService.updateCompany(principal.memberId(), companyId, req);
        return ResponseEntity.ok(res);
    }

    /**
     * 초대코드 재생성
     */
    @PostMapping("/{companyId}/invite-code/regenerate")
    public ResponseEntity<UpdateCompanyResponse> regenerateInviteCode(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        var res = companyService.regenerateInviteCode(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     *  회사 삭제
     */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        companyService.deleteCompany(principal.memberId(), companyId);
        return ResponseEntity.noContent().build();
    }
}
