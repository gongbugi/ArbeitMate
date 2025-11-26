package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.response.SalaryResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/salary")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    // 내 예상 급여 조회 API
    // GET /companies/{companyId}/salary?year=2025&month=11
    @GetMapping
    public ResponseEntity<SalaryResponse> getMySalary(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @RequestParam int year,
            @RequestParam int month) {

        var response = salaryService.calculateMonthlySalary(principal.memberId(), companyId, year, month);
        return ResponseEntity.ok(response);
    }
}