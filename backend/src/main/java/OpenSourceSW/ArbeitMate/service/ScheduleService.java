package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyRole;
import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import OpenSourceSW.ArbeitMate.dto.request.CreateMonthlyPeriodRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateScheduleSlotRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateScheduleSlotsRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateWeeklyPeriodRequest;
import OpenSourceSW.ArbeitMate.dto.response.SchedulePeriodResponse;
import OpenSourceSW.ArbeitMate.dto.response.ScheduleSlotResponse;
import OpenSourceSW.ArbeitMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyMemberRoleRepository companyMemberRoleRepository;
    private final ScheduleRepository scheduleRepository;
    private final SchedulePeriodRepository schedulePeriodRepository;

    /**
     * 주간 스케쥴 기간 생성
     */
    @Transactional
    public SchedulePeriodResponse createWeeklyPeriod(UUID ownerId, UUID companyId, CreateWeeklyPeriodRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        LocalDate start = req.getBaseDate();
        LocalDate end = start.plusDays(6);

        validateNoPeriodOverlap(companyId, start, end);

        String name = normalizeNameOrGenerateWeekly(companyId, start, req.getName());

        SchedulePeriod period = SchedulePeriod.create(
                company,
                name,
                PeriodType.WEEKLY,
                start,
                end,
                req.getAvailabilityDueAt()
        );

        schedulePeriodRepository.save(period);

        return SchedulePeriodResponse.from(period);
    }

    /**
     * 월간 스케쥴 기간 생성
     */
    @Transactional
    public SchedulePeriodResponse createMonthlyPeriod(UUID ownerId, UUID companyId, CreateMonthlyPeriodRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        LocalDate start = LocalDate.of(req.getYear(), req.getMonth(), 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        validateNoPeriodOverlap(companyId, start, end);

        String name = normalizeNameOrGenerateMonthly(companyId, start, req.getName());

        SchedulePeriod period = SchedulePeriod.create(
                company,
                name,
                PeriodType.MONTHLY,
                start,
                end,
                req.getAvailabilityDueAt()
        );

        schedulePeriodRepository.save(period);

        return SchedulePeriodResponse.from(period);
    }

    /**
     * 생성된 기간내에 날짜 시간대별 슬롯 생성
     */
    @Transactional
    public List<ScheduleSlotResponse> createSlots(UUID ownerId, UUID companyId, UUID periodId, CreateScheduleSlotsRequest req) {
        /// 유효성 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }
        if (period.getStatus() != PeriodStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태의 기간에서만 슬롯을 생성할 수 있습니다.");
        }

        /// 스케쥴 슬롯 생성
        List<Schedule> schedules = new ArrayList<>();
        for (CreateScheduleSlotRequest slotReq : req.getSlots()) {
            LocalDate workDate = slotReq.getWorkDate();

            // 기간 범위 체크
            if (workDate.isBefore(period.getStartDate()) || workDate.isAfter(period.getEndDate())) {
                throw new IllegalArgumentException("기간 범위를 벗어난 날짜입니다.");
            }

            // 역할군 조회 및 회사 일치 여부 검증
            CompanyRole role = companyRoleRepository.findById(slotReq.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            if (!role.getCompany().getId().equals(companyId)) {
                throw new IllegalStateException("해당 매장의 역할군이 아닙니다.");
            }

            // 엔티티 생성 (시간/인원수 검증 포함)
            Schedule s = Schedule.create(
                    company,
                    period,
                    role,
                    workDate,
                    slotReq.getStartTime(),
                    slotReq.getEndTime(),
                    slotReq.getRequiredHeadCount()
            );

            schedules.add(s);
        }

        List<Schedule> created = scheduleRepository.saveAll(schedules);

        return created.stream()
                .map(ScheduleSlotResponse::from)
                .toList();
    }


    /// ====== 이름 생성 및 중복 확인 ======
    private String normalizeNameOrGenerateWeekly(UUID companyId, LocalDate start, String rawName) {
        String name = (rawName != null && !rawName.isBlank()) ? rawName.trim() : generateWeeklyName(start);

        if (schedulePeriodRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new IllegalStateException("이미 동일한 이름의 기간이 존재합니다.");
        }
        return name;
    }
    private String normalizeNameOrGenerateMonthly(UUID companyId, LocalDate start, String rawName) {
        String name = (rawName != null && !rawName.isBlank()) ? rawName.trim() : generateMonthlyName(start);

        if (schedulePeriodRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new IllegalStateException("이미 동일한 이름의 기간이 존재합니다.");
        }
        return name;
    }

    private String generateWeeklyName(LocalDate start) {
        WeekFields wf = WeekFields.of(Locale.KOREA);
        int week = start.get(wf.weekOfWeekBasedYear());
        return start.getYear() + "-W" + week; // ex) 2025-W36
    }
    private String generateMonthlyName(LocalDate start) {
        int year = start.getYear();
        int month = start.getMonthValue();
        return String.format("%04d-%02d", year, month); // ex) 2025-11
    }

    /// ====== 검증 메서드 ======
    // Owner 확인
    private void validateOwner(UUID memberId, Company company) {
        if (!company.getOwner().getId().equals(memberId)) {
            throw new IllegalStateException("해당 매장의 사장만 이 작업을 수행할 수 있습니다.");
        }
    }
    // 회사 소속 확인
    private void validateMembersBelongToCompany(UUID memberId, Company company) {
        if(company.getCompanyMembers().stream() .noneMatch(cm -> cm.getMember().getId().equals(memberId))) {
            throw new IllegalStateException("해당 매장에 속한 멤버가 아닙니다.");
        }
    }
    // 기간 겹침 확인
    private void validateNoPeriodOverlap(UUID companyId, LocalDate start, LocalDate end) {
        if (schedulePeriodRepository.existsOverlapping(companyId, start, end)) {
            throw new IllegalStateException("해당 기간에 이미 다른 스케줄 기간이 존재합니다.");
        }
    }
}
