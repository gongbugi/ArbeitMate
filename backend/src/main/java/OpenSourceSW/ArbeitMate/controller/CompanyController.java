package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CreateCompanyResponse;
import OpenSourceSW.ArbeitMate.dto.response.ParticipateCompanyResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
