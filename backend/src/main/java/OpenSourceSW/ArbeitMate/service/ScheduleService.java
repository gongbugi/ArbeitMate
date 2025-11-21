package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.*;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import OpenSourceSW.ArbeitMate.dto.request.*;
import OpenSourceSW.ArbeitMate.dto.response.SchedulePeriodResponse;
import OpenSourceSW.ArbeitMate.dto.response.SchedulePeriodWithSlotsResponse;
import OpenSourceSW.ArbeitMate.dto.response.ScheduleSlotResponse;
import OpenSourceSW.ArbeitMate.dto.response.StaffingTemplateResponse;
import OpenSourceSW.ArbeitMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

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
    private final StaffingTemplateRepository staffingTemplateRepository;

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
     * 특정 스케쥴 기간과 그 기간에 속한 모든 슬롯 조회
     */
    public SchedulePeriodWithSlotsResponse getPeriodWithSlots(UUID memberId, UUID companyId, UUID periodId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // 회사 소속 멤버인지 확인
        validateMembersBelongToCompany(memberId, company);

        // 기간 조회
        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }

        // 해당 기간의 슬롯 조회
        List<Schedule> schedules = scheduleRepository.findByPeriod(period);

        return SchedulePeriodWithSlotsResponse.of(period, schedules);
    }

    /**
     * 스케쥴 기간 및 슬롯 전체 조회
     */
    public List<SchedulePeriodWithSlotsResponse> listPeriodsWithSlots(UUID ownerId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        List<SchedulePeriod> periods = schedulePeriodRepository.findByCompanyIdOrderByStartDateAsc(companyId);

        return periods.stream()
                .map(period -> {
                    List<Schedule> schedules = scheduleRepository.findByPeriod(period);
                    return SchedulePeriodWithSlotsResponse.of(period, schedules);
                })
                .toList();
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

    /**
     * 필요 인원 템플릿 생성
     */
    @Transactional
    public StaffingTemplateResponse createStaffingTemplate(UUID ownerId, UUID companyId, CreateStaffingTemplateRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        String name = req.getName().trim();
        if (staffingTemplateRepository.existsByCompanyIdAndName(companyId, name)) {
            throw new IllegalStateException("이미 동일한 이름의 템플릿이 존재합니다.");
        }

        // 템플릿 헤더 생성 (생성자는 사장님 전용 기능이기에 사장으로 자동 설정)
        var template = StaffingTemplate.create(company, name, company.getOwner());

        // 각 슬롯 아이템 생성
        req.getItems().forEach(itemReq -> {
            var role = companyRoleRepository.findById(itemReq.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));

            if (!role.getCompany().getId().equals(companyId)) {
                throw new IllegalStateException("해당 매장의 역할군이 아닙니다.");
            }

            StaffingTemplateItem item = StaffingTemplateItem.create(
                    template,
                    role,
                    itemReq.getDow(),
                    itemReq.getStartTime(),
                    itemReq.getEndTime(),
                    itemReq.getRequiredHeadCount()
            );
        });

        staffingTemplateRepository.save(template);

        return StaffingTemplateResponse.from(template);
    }

    /**
     * 회사에 등록된 필요 인원 템플릿 목록 조회
     */
    public List<StaffingTemplateResponse> listStaffingTemplates(UUID ownerId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        return staffingTemplateRepository.findByCompanyId(companyId).stream()
                .map(StaffingTemplateResponse::from)
                .toList();
    }

    /**
     * 저장된 템플릿을 지정된 스케쥴 기간에 적용
     */
    @Transactional
    public List<ScheduleSlotResponse> applyTemplateToPeriod(UUID ownerId, UUID companyId, UUID periodId, UUID templateId) {
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
            throw new IllegalStateException("DRAFT 상태의 기간에서만 템플릿을 적용할 수 있습니다.");
        }

        StaffingTemplate template = staffingTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        if (!template.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 템플릿이 아닙니다.");
        }

        /// 기존 기간 내 스케쥴이 있다면 삭제
        scheduleRepository.deleteByPeriodId(period.getId());

        /// 템플릿 기반 스케쥴 생성
        List<Schedule> newSchedules = new ArrayList<>();

        LocalDate date = period.getStartDate();
        while (!date.isAfter(period.getEndDate())) {

            // 0=월..6=일 로 변환
            int dow = date.getDayOfWeek().getValue() - 1;

            for (StaffingTemplateItem item : template.getItems()) {
                if (item.getDow() != dow) continue;

                Schedule s = Schedule.create(
                        company,
                        period,
                        item.getRole(),
                        date,
                        item.getStartTime(),
                        item.getEndTime(),
                        item.getHeadcount()
                );
                newSchedules.add(s);
            }

            date = date.plusDays(1);
        }

        List<Schedule> created = scheduleRepository.saveAll(newSchedules);

        return created.stream()
                .map(ScheduleSlotResponse::from)
                .toList();
    }

    /**
     * 기존에 만들었던 SchedulePeriod에 편성된 스케쥴 기반 템플릿 생성
     */
    @Transactional
    public StaffingTemplateResponse createTemplateFromPeriod(UUID ownerId, UUID companyId, UUID periodId) {
        /// 유효성 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }

        // 해당 기간의 모든 슬롯 조회
        List<Schedule> schedules = scheduleRepository.findByPeriod(period);
        if (schedules.isEmpty()) {
            throw new IllegalStateException("해당 기간에는 스케쥴 슬롯이 없습니다. 템플릿을 생성할 수 없습니다.");
        }

        /// 템플릿 자동 생성
        // 템플릿 이름 자동 생성
        String templateName = generateTemplateNameFromPeriod(companyId, period);

        // 템플릿 엔티티 생성
        StaffingTemplate template = StaffingTemplate.create(company, templateName, company.getOwner());

        // (dow, roleId, startTime, endTime) 기준으로 패턴 집계
        record Key(int dow, UUID roleId, java.time.LocalTime start, java.time.LocalTime end) {}

        Map<Key, Integer> headcountMap = new LinkedHashMap<>();

        for (Schedule s : schedules) {
            LocalDate date = s.getWorkDate();
            int dow = date.getDayOfWeek().getValue() - 1; // 월(1) -> 0

            Key key = new Key(
                    dow,
                    s.getRole().getId(),
                    s.getStartTime(),
                    s.getEndTime()
            );

            int hc = s.getRequiredHeadcount();
            headcountMap.merge(key, hc, Math::max);
        }

        // 집계된 패턴을 기반으로 TemplateItem 생성
        for (Map.Entry<Key, Integer> entry : headcountMap.entrySet()) {
            Key k = entry.getKey();
            int headcount = entry.getValue();

            CompanyRole role = companyRoleRepository.findById(k.roleId())
                    .orElseThrow(() -> new IllegalStateException("템플릿 생성 중 해당 role을 찾을 수 없었습니다."));

            StaffingTemplateItem.create(
                    template,
                    role,
                    k.dow(),
                    k.start(),
                    k.end(),
                    headcount
            );
        }

        staffingTemplateRepository.save(template);

        return StaffingTemplateResponse.from(template);
    }

    /**
     * 템플릿 삭제 기능
     */
    @Transactional
    public void deleteTemplate(UUID ownerId, UUID companyId, UUID templateId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        // 템플릿 조회
        StaffingTemplate template = staffingTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        // 템플릿이 해당 매장 소속인지 확인
        if (!template.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 템플릿이 아닙니다.");
        }

        staffingTemplateRepository.delete(template); // 삭제 (items는 orphanRemoval로 같이 삭제)
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

    private String generateTemplateNameFromPeriod(UUID companyId, SchedulePeriod period) {
        String base;
        if (period.getName() != null && !period.getName().isBlank()) {
            base = period.getName().trim();
        } else {
            base = String.format("PERIOD_%s_%s", period.getStartDate(), period.getEndDate());
        }

        String name = base + "-TEMPLATE";
        int suffix = 2;

        while (staffingTemplateRepository.existsByCompanyIdAndName(companyId, name)) {
            name = base + "-TEMPLATE-" + suffix;
            suffix++;
        }

        return name; // ex) <기존이름>-TEMPLATE
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
