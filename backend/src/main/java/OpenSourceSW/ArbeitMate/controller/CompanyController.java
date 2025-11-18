package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.*;
import OpenSourceSW.ArbeitMate.dto.response.*;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            @Valid @RequestBody UpdateCompanyRequest req) {

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

    /**
     * 회사 직원 목록 조회 (사장 전용)
     */
    @GetMapping("/{companyId}/workers")
    public ResponseEntity<List<CompanyWorkerResponse>> listWorkers(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {
        var res = companyService.listWorkers(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 특정 직원 매장에서 제외 (사장 전용)
     */
    @DeleteMapping("/{companyId}/workers/{companyMemberId}")
    public ResponseEntity<Void> removeWorker(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID companyMemberId) {
        companyService.removeWorker(principal.memberId(), companyId, companyMemberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회사 역할군 추가 (사장 전용)
     */
    @PostMapping("/{companyId}/roles")
    public ResponseEntity<CompanyRoleResponse> createRole(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateRoleRequest req) {
        var res = companyService.createRole(principal.memberId(), companyId, req);
        return ResponseEntity.ok(res);
    }

    /**
     * 회사 역할군 목록 조회
     */
    @GetMapping("/{companyId}/roles")
    public ResponseEntity<List<CompanyRoleResponse>> listRoles(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {
        var res = companyService.listRoles(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 직원에게 역할군 부여
     */
    @PostMapping("/{companyId}/workers/{companyMemberId}/roles")
    public ResponseEntity<Void> assignRoleToWorker(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID companyMemberId,
            @Valid @RequestBody AssignRoleRequest req) {
        companyService.assignRoleToWorker(principal.memberId(), companyId, companyMemberId, req.getRoleId());
        return ResponseEntity.status(500).build();
    }
}
