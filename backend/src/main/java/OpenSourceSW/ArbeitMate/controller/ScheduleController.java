package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.*;
import OpenSourceSW.ArbeitMate.dto.response.*;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 사장 전용
 */
@RestController
@RequestMapping("/companies/{companyId}/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 주간 스케쥴 기간 생성
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
     * 월간 스케쥴 기간 생성
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
     * 스케쥴 기간 내 날짜/시간대별 슬롯 생성
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

    /**
     * 특정 기간 + 슬롯 조회
     */
    @GetMapping("/periods/{periodId}")
    public ResponseEntity<SchedulePeriodWithSlotsResponse> getPeriodWithSlots(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        var res = scheduleService.getPeriodWithSlots(principal.memberId(), companyId, periodId);
        return ResponseEntity.ok(res);
    }

    /**
     * 매장의 모든 기간 + 각 기간별 슬롯 목록 조회
     */
    @GetMapping("/periods-with-slots")
    public ResponseEntity<List<SchedulePeriodWithSlotsResponse>> listPeriodsWithSlots(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        var res = scheduleService.listPeriodsWithSlots(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 근무자 희망 근무 요일/시간 조회
     */
    @GetMapping("/worker/availability-pattern")
    public ResponseEntity<MemberAvailabilityResponse> getMemberAvailabilityPattern(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        var res = scheduleService.getMemberAvailabilityPattern(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 근무지 희망 근무 요일/시간 제출(수정)
     */
    @PostMapping("/worker/availability-pattern")
    public ResponseEntity<Void> updateMemberAvailabilityPattern(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @RequestBody @Valid UpdateMemberAvailabilityRequest req) {

        scheduleService.updateMemberAvailabilityPattern(principal.memberId(), companyId, req);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 필요 인원 템플릿 생성
     */
    @PostMapping("/create/staffing-templates")
    public ResponseEntity<StaffingTemplateResponse> createStaffingTemplate(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateStaffingTemplateRequest req) {

        var res = scheduleService.createStaffingTemplate(principal.memberId(), companyId, req);
        return ResponseEntity.ok(res);
    }

    /**
     * 기존에 사용했던 SchedulePeriod 기반 자동 템플릿 생성
     */
    @PostMapping("/{periodId}/create-auto/staffing-templates")
    public ResponseEntity<StaffingTemplateResponse> createTemplateFromPeriod(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        var res = scheduleService.createTemplateFromPeriod(principal.memberId(), companyId, periodId);
        return ResponseEntity.ok(res);
    }

    /**
     * 회사의 템플릿 목록 조회
     */
    @GetMapping("/staffing-templates")
    public ResponseEntity<List<StaffingTemplateResponse>> listStaffingTemplates(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId) {

        var res = scheduleService.listStaffingTemplates(principal.memberId(), companyId);
        return ResponseEntity.ok(res);
    }

    /**
     * 특정 기간에 템플릿 적용
     */
    @PostMapping("/{periodId}/apply-template/{templateId}")
    public ResponseEntity<List<ScheduleSlotResponse>> applyTemplateToPeriod(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId,
            @PathVariable UUID templateId) {

        var res = scheduleService.applyTemplateToPeriod(principal.memberId(), companyId, periodId, templateId);
        return ResponseEntity.ok(res);
    }

    /**
     * 템플릿 삭제
     */
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID templateId) {
        scheduleService.deleteTemplate(principal.memberId(), companyId, templateId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 가능한 스케쥴 슬롯 목록 조회
     */
    @GetMapping("/{periodId}/availability/slots")
    public ResponseEntity<WorkerAvailabilitySlotsResponse> getWorkerSlots(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        var res =  scheduleService.getWorkerAvailabilitySlots(principal.memberId(), companyId, periodId);
        return ResponseEntity.ok(res);
    }

    /**
     * 가용 시간 제출 (근무자)
     */
    @PostMapping("/{periodId}/availability/submit")
    public ResponseEntity<Void> submitAvailability(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId,
            @RequestBody SubmitAvailabilityRequest req) {

        scheduleService.submitAvailability(principal.memberId(), companyId, periodId, req);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 근무자들 가용 시간 제출 현황 조회
     */
    @GetMapping("/{periodId}/availability/submissions")
    public ResponseEntity<List<AvailabilitySubmissionStatusResponse>> getSubmissionStatus(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        var res = scheduleService.getAvailabilitySubmissionStatus(principal.memberId(), companyId, periodId);
        return ResponseEntity.ok(res);
    }

    /**
     * 자동 편성 실행
     */
    @PostMapping("/{periodId}/auto-assign")
    public ResponseEntity<List<ScheduleAssignmentSlotResponse>> autoAssignSchedules(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        var result = scheduleService.autoAssignSchedules(principal.memberId(), companyId, periodId);
        return ResponseEntity.ok(result);
    }

    /**
     * 수동 편성 반영
     */
    @PutMapping("/{periodId}/assignments")
    public ResponseEntity<List<ScheduleAssignmentSlotResponse>> updateScheduleAssignments(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId,
            @Valid @RequestBody UpdateScheduleAssignmentsRequest request) {

        var result = scheduleService.updateScheduleAssignments(principal.memberId(), companyId, periodId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * 근무표 확정 (게시)
     */
    @PostMapping("/{periodId}/publish")
    public ResponseEntity<Void> publishSchedulePeriod(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @PathVariable UUID periodId) {

        scheduleService.publishSchedulePeriod(principal.memberId(), companyId, periodId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
