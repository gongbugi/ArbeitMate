package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.UpdateFixedShiftRequest;
import OpenSourceSW.ArbeitMate.dto.response.FixedShiftResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/companies/{companyId}/workers/fixed-shifts")
public class FixedShiftController {

    private final ScheduleService scheduleService;

    /**
     * 전체 고정 근무자 설정 조회
     */
    @GetMapping
    public ResponseEntity<List<FixedShiftResponse>> getAllFixedShiftConfig(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        var res = scheduleService.getAllFixedShiftConfig(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 특정 고정 근무자 설정 조회
     */
    @GetMapping("/{companyMemberId}")
    public ResponseEntity<FixedShiftResponse> getFixedShiftConfig(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID companyMemberId) {

        var res = scheduleService.getFixedShiftConfig(principal.memberId(), companyId, companyMemberId);
        return ResponseEntity.ok(res);
    }

    /**
     * 고정 근무자 설정/변경
     */
    @PostMapping("/{companyMemberId}")
    public ResponseEntity<FixedShiftResponse> updateFixedShiftConfig(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID companyMemberId,
            @RequestBody UpdateFixedShiftRequest req) {

        var res =  scheduleService.updateFixedShift(principal.memberId(), companyId, companyMemberId, req);
        return ResponseEntity.ok(res);
    }
}
