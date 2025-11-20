package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.CreateMonthlyPeriodRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateScheduleSlotsRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateWeeklyPeriodRequest;
import OpenSourceSW.ArbeitMate.dto.response.SchedulePeriodResponse;
import OpenSourceSW.ArbeitMate.dto.response.ScheduleSlotResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/compaines/{companyId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 주간 스케쥴 기간 생성 (사장 전용)
     */
    @PostMapping("/create/weekly")
    public ResponseEntity<SchedulePeriodResponse> createWeeklySchedule(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateWeeklyPeriodRequest req) {

        var res = scheduleService.createWeeklyPeriod(principal.memberId(), companyId, req);
        return ResponseEntity.ok(res);
    }

    /**
     * 월간 스케쥴 기간 생성 (사장 전용)
     */
    @PostMapping("/create/monthly")
    public ResponseEntity<SchedulePeriodResponse> createMonthlySchedule(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateMonthlyPeriodRequest req) {

        var res = scheduleService.createMonthlyPeriod(principal.memberId(), companyId, req);
        return ResponseEntity.ok(res);
    }

    /**
     * 스케쥴 기간 내 날짜/시간대별 1개 이상의 슬롯 생성
     */
    @PostMapping("/{periodId}/create/slots")
    public ResponseEntity<List<ScheduleSlotResponse>> createSlots(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId,
            @Valid @RequestBody CreateScheduleSlotsRequest req) {

        var res = scheduleService.createSlots(principal.memberId(), companyId, periodId, req);
        return ResponseEntity.ok(res);
    }
}
