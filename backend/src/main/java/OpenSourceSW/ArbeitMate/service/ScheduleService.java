package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.*;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import OpenSourceSW.ArbeitMate.dto.request.*;
import OpenSourceSW.ArbeitMate.dto.response.*;
import OpenSourceSW.ArbeitMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;

    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyMemberRoleRepository companyMemberRoleRepository;

    private final FixedShiftRepository fixedShiftRepository;

    private final ScheduleRepository scheduleRepository;
    private final SchedulePeriodRepository schedulePeriodRepository;
    private final StaffingTemplateRepository staffingTemplateRepository;

    private final MemberAvailabilityRepository memberAvailabilityRepository;
    private final ScheduleSlotAvailabilityRepository scheduleSlotAvailabilityRepository;
    private final AvailabilitySubmissionRepository availabilitySubmissionRepository;

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

    /**
     * 근무자 희망 근무 시간 등록/갱신
     */
    @Transactional
    public void updateMemberAvailabilityPattern(UUID memberId, UUID companyId, UpdateMemberAvailabilityRequest req) {
        // 회사 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // 회사 소속 멤버인지 확인
        CompanyMember cm = companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId)
                .orElseThrow(() -> new IllegalStateException("해당 매장에 속한 멤버가 아닙니다."));

        // 고정 근무자는 희망 근무 시간 등록 불가
        if (cm.isFixedShiftWorker()) {
            throw new IllegalStateException("고정 근무자는 희망 근무 시간을 별도로 등록할 수 없습니다.");
        }

        Member member = cm.getMember();

        // 기존 패턴 전체 삭제
        memberAvailabilityRepository.deleteByCompanyIdAndMemberId(companyId, member.getId());

        // 요청이 비어 있으면 "전부 삭제" 상태로 종료
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return;
        }

        // 새 패턴 생성
        List<MemberAvailability> created = new ArrayList<>();
        for (MemberAvailabilityItemRequest item : req.getItems()) {

            // dow 범위 추가 검증 (0~6)
            if (item.getDow() < 0 || item.getDow() > 6) {
                throw new IllegalArgumentException("dow 값은 0(월) ~ 6(일) 이어야 합니다.");
            }

            MemberAvailability avail = MemberAvailability.create(
                    company,
                    member,
                    item.getDow(),
                    item.getStartTime(),
                    item.getEndTime(),
                    item.getEffectiveFrom(),
                    item.getEffectiveTo()
            );

            created.add(avail);
        }

        memberAvailabilityRepository.saveAll(created);
    }

    /**
     * 근무자 희망 근무 시간 조회
     */
    public MemberAvailabilityResponse getMemberAvailabilityPattern(UUID memberId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        CompanyMember cm = companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId)
                .orElseThrow(() -> new IllegalStateException("해당 매장에 속한 멤버가 아닙니다."));

        if (cm.isFixedShiftWorker()) {
            throw new IllegalStateException("고정 근무자는 희망 근무 시간 패턴을 별도로 사용하지 않습니다.");
        }

        Member member = cm.getMember();

        List<MemberAvailability> list = memberAvailabilityRepository.findByCompanyIdAndMemberId(companyId, member.getId());

        List<MemberAvailabilityItemResponse> items = list.stream()
                .map(MemberAvailabilityItemResponse::from)
                .toList();

        return MemberAvailabilityResponse.builder()
                .memberId(member.getId())
                .companyId(companyId)
                .memberName(member.getName())
                .items(items)
                .build();
    }

    /**
     * 고정 근무자 설정 및 고정 근무 패턴 등록(갱신)
     */
    @Transactional
    public FixedShiftResponse updateFixedShift(UUID ownerId, UUID companyId, UUID companyMemberId, UpdateFixedShiftRequest req) {
        // 1. 회사 + 사장 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        // 2. 회사 소속 검증
        CompanyMember cm = companyMemberRepository.findById(companyMemberId)
                .orElseThrow(() -> new IllegalArgumentException("CompanyMember not found"));
        if (!cm.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 직원이 아닙니다.");
        }

        Member member = cm.getMember();

        // 3. fixedShiftWorker=false 인 경우: 플래그 해제 + 기존 패턴 삭제하고 끝
        if (!req.isFixedShiftWorker()) {
            cm.unmarkAsFixedShiftWorker();
            fixedShiftRepository.deleteByCompanyIdAndMemberId(companyId, member.getId());

            return buildFixedShiftResponse(cm, false, List.of());
        }

        // 4. fixedShiftWorker=true 인 경우: 기존 패턴 삭제 후 새로 생성(갱신)
        cm.markAsFixedShiftWorker();
        fixedShiftRepository.deleteByCompanyIdAndMemberId(companyId, member.getId());

        List<FixedShift> created = new ArrayList<>();

        if (req.getShifts() != null) {
            for (FixedShiftItemRequest item : req.getShifts()) {
                CompanyRole role = companyRoleRepository.findById(item.getRoleId())
                        .orElseThrow(() -> new IllegalArgumentException("Role not found"));

                if (!role.getCompany().getId().equals(companyId)) {
                    throw new IllegalStateException("해당 매장의 역할군이 아닙니다.");
                }

                FixedShift fs = FixedShift.create(
                        company,
                        member,
                        role,
                        item.getDow(),
                        item.getStartTime(),
                        item.getEndTime(),
                        item.getEffectiveFrom(),
                        item.getEffectiveTo()
                );
                created.add(fs);
            }
        }

        fixedShiftRepository.saveAll(created);

        List<FixedShiftItemResponse> itemResponses = created.stream()
                .map(FixedShiftItemResponse::from)
                .toList();

        return buildFixedShiftResponse(cm, true, itemResponses);
    }

    /**
     * 전체 고정 근무자 설정/패턴 조회
     */
    public List<FixedShiftResponse> getAllFixedShiftConfig(UUID ownerId, UUID companyId) {
        // 회사 + 사장 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        validateOwner(ownerId, company);

        // 이 매장의 WORKER들 중 고정 근무자만 필터링
        List<CompanyMember> workers = companyMemberRepository
                .findByCompanyIdAndRole(companyId, MembershipRole.WORKER);

        List<CompanyMember> fixedWorkers = workers.stream()
                .filter(CompanyMember::isFixedShiftWorker)
                .toList();

        if (fixedWorkers.isEmpty()) {
            return List.of();
        }

        // 회사 기준 전체 FixedShift 로드 후 멤버별로 그룹핑
        List<FixedShift> allShifts = fixedShiftRepository.findByCompanyId(companyId);
        Map<UUID, List<FixedShift>> shiftsByMember = allShifts.stream()
                .collect(Collectors.groupingBy(fs -> fs.getMember().getId()));

        // 각 고정 근무자별로 FixedShiftResponse 생성
        return fixedWorkers.stream()
                .map(cm -> {
                    UUID memberId = cm.getMember().getId();
                    List<FixedShift> shifts = shiftsByMember.getOrDefault(memberId, List.of());

                    List<FixedShiftItemResponse> itemResponses = shifts.stream()
                            .map(FixedShiftItemResponse::from)
                            .toList();

                    return buildFixedShiftResponse(cm, true, itemResponses);
                }).toList();
    }

    /**
     * 특정 고정 근무자 설정/패턴 조회
     */
    public FixedShiftResponse getFixedShiftConfig(UUID ownerId, UUID companyId, UUID companyMemberId) {
        // 회사 + 사장 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        validateOwner(ownerId, company);

        CompanyMember cm = companyMemberRepository.findById(companyMemberId)
                .orElseThrow(() -> new IllegalArgumentException("CompanyMember not found"));

        if (!cm.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 직원이 아닙니다.");
        }

        Member member = cm.getMember();

        List<FixedShift> shifts = fixedShiftRepository.findByCompanyIdAndMemberId(companyId, member.getId());
        List<FixedShiftItemResponse> itemResponses = shifts.stream()
                .map(FixedShiftItemResponse::from)
                .toList();

        boolean fixed = cm.isFixedShiftWorker();

        return buildFixedShiftResponse(cm, fixed, itemResponses);
    }

    /**
     * 스케쥴 슬롯 조회 (제출한 가능 시간 기반으로 추천 + 역할 기반 필터링)
     */
    public WorkerAvailabilitySlotsResponse getWorkerAvailabilitySlots(UUID memberId, UUID companyId, UUID periodId) {
        /// 유효성 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }

        // 해당 회사에 속한 멤버인지 + CompanyMember 조회
        CompanyMember cm = companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId)
                .orElseThrow(() -> new IllegalStateException("해당 매장에 속한 멤버가 아닙니다."));

        // 고정 근무자는 가용 시간 제출 대상에서 제외
        if (cm.isFixedShiftWorker()) {
            throw new IllegalStateException("고정 근무자는 가용 시간 제출 대상이 아닙니다.");
        }

        /// 슬롯 조회 및 필터링
        Member member = cm.getMember();

        // 이 멤버가 수행 가능한 역할 목록
        List<CompanyMemberRole> memberRoles = companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, member.getId());
        if (memberRoles.isEmpty()) {
            // 역할이 하나도 없으면 빈 결과 반환
            return WorkerAvailabilitySlotsResponse.builder()
                    .recommendedSlots(List.of())
                    .otherSlots(List.of())
                    .build();
        }

        Set<UUID> allowedRoleIds = memberRoles.stream()
                .map(cmr -> cmr.getRole().getId())
                .collect(Collectors.toSet());

        // 해당 기간의 모든 스케줄 슬롯
        List<Schedule> allSlots = scheduleRepository.findByPeriod(period);

        // 이 멤버가 가능한 역할에 해당하는 슬롯만 필터링
        List<Schedule> candidateSlots = allSlots.stream()
                .filter(s -> allowedRoleIds.contains(s.getRole().getId()))
                .toList();

        // MemberAvailability 패턴
        List<MemberAvailability> patterns = memberAvailabilityRepository.findByCompanyIdAndMemberId(companyId, member.getId());

        // 이미 제출한 슬롯 (willing=true) 조회
        List<ScheduleSlotAvailability> existingAvail = scheduleSlotAvailabilityRepository.findByMemberAndPeriod(member.getId(), period);
        Set<UUID> willingSlotIds = existingAvail.stream()
                .filter(ScheduleSlotAvailability::isWilling)
                .map(a -> a.getSchedule().getId())
                .collect(Collectors.toSet());

        // 슬롯별 recommended / willing 계산
        List<WorkerSlotResponse> allWorkerSlots = candidateSlots.stream()
                .map(slot -> {
                    LocalDate date = slot.getWorkDate();
                    int dow = date.getDayOfWeek().getValue() - 1; // 0=월..6=일

                    boolean recommended = patterns.stream().anyMatch(p -> p.getDow() == dow && p.isEffectiveOn(date) && p.overlaps(slot.getStartTime(), slot.getEndTime()));

                    boolean willing = willingSlotIds.contains(slot.getId());

                    return WorkerSlotResponse.builder()
                            .scheduleId(slot.getId())
                            .workDate(slot.getWorkDate())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .roleId(slot.getRole().getId())
                            .roleName(slot.getRole().getName())
                            .recommended(recommended)
                            .willing(willing)
                            .build();
                })
                .toList();

        // recommended / others 분리 + 정렬
        List<WorkerSlotResponse> recommendedSlots = allWorkerSlots.stream()
                .filter(WorkerSlotResponse::isRecommended)
                .sorted(Comparator.comparing(WorkerSlotResponse::getWorkDate)
                        .thenComparing(WorkerSlotResponse::getStartTime))
                .toList();

        List<WorkerSlotResponse> otherSlots = allWorkerSlots.stream()
                .filter(s -> !s.isRecommended())
                .sorted(Comparator
                        .comparing(WorkerSlotResponse::getWorkDate)
                        .thenComparing(WorkerSlotResponse::getStartTime))
                .toList();

        return WorkerAvailabilitySlotsResponse.builder()
                .recommendedSlots(recommendedSlots)
                .otherSlots(otherSlots)
                .build();
    }

    /**
     * 근무자 가용 시간 제출
     */
    @Transactional
    public void submitAvailability(UUID memberId, UUID companyId, UUID periodId, SubmitAvailabilityRequest req) {
        /// 유효성 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }
        if (period.getStatus() != PeriodStatus.DRAFT) {
            throw new IllegalStateException("DRAFT 상태의 기간에서만 가용 시간을 제출할 수 있습니다.");
        }
        if (period.getAvailabilityDueAt() != null && java.time.LocalDateTime.now().isAfter(period.getAvailabilityDueAt())) {
            throw new IllegalStateException("가용 시간 제출 마감 시간이 지났습니다.");
        }

        CompanyMember cm = companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId)
                .orElseThrow(() -> new IllegalStateException("해당 매장에 속한 멤버가 아닙니다."));

        if (cm.isFixedShiftWorker()) {
            throw new IllegalStateException("고정 근무자는 가용 시간 제출 대상이 아닙니다.");
        }

        Member member = cm.getMember();

        // 멤버의 가능한 역할 목록
        var memberRoles = companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, member.getId());
        var allowedRoleIds = memberRoles.stream()
                .map(cmr -> cmr.getRole().getId())
                .collect(java.util.stream.Collectors.toSet());

        // 요청으로 넘어온 슬롯들 로드
        var slotIds = (req.getSlotIds() == null) ? List.<UUID>of() : req.getSlotIds();
        var slots = slotIds.isEmpty()
                ? List.<Schedule>of()
                : scheduleRepository.findAllById(slotIds);

        // 유효성 검증: 해당 company, period, 역할
        for (Schedule s : slots) {
            if (!s.getPeriod().getId().equals(periodId)) {
                throw new IllegalStateException("다른 기간의 스케쥴 슬롯이 포함되어 있습니다.");
            }
            if (!s.getCompany().getId().equals(companyId)) {
                throw new IllegalStateException("다른 매장의 스케쥴 슬롯이 포함되어 있습니다.");
            }
            if (!allowedRoleIds.contains(s.getRole().getId())) {
                throw new IllegalStateException("해당 멤버가 수행할 수 없는 역할의 슬롯이 포함되어 있습니다.");
            }
        }

        // 이전 제출 내용 제거
        scheduleSlotAvailabilityRepository.deleteByMemberAndPeriod(member.getId(), period);

        // 이번 제출 내용 생성
        var newAvail = slots.stream()
                .map(s -> ScheduleSlotAvailability.willing(s, member))
                .toList();

        scheduleSlotAvailabilityRepository.saveAll(newAvail);

        // AvailabilitySubmission 갱신
        Optional<AvailabilitySubmission> opt = availabilitySubmissionRepository
                .findByCompanyIdAndPeriodIdAndMemberId(companyId, periodId, member.getId());

        if (opt.isPresent()) {
            opt.get().refreshSubmittedAt();
        } else {
            AvailabilitySubmission sub = AvailabilitySubmission.create(company, period, member);
            availabilitySubmissionRepository.save(sub);
        }
    }

    /**
     * 근무자들 가능 여부 제출 현황 조회
     */
    public List<AvailabilitySubmissionStatusResponse> getAvailabilitySubmissionStatus(UUID ownerId, UUID companyId, UUID periodId) {
        /// 유효성 검증
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        SchedulePeriod period = schedulePeriodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("SchedulePeriod not found"));

        if (!period.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 스케쥴 기간이 아닙니다.");
        }

        // 이 매장의 WORKER들 (OWNER 제외)
        List<CompanyMember> workers = companyMemberRepository.findByCompanyIdAndRole(companyId, MembershipRole.WORKER);

        // 고정 근무자는 제출 대상에서 제외
        List<CompanyMember> targetWorkers = workers.stream()
                .filter(cm -> !cm.isFixedShiftWorker())
                .toList();

        // 해당 기간의 제출 기록
        List<AvailabilitySubmission> submissions = availabilitySubmissionRepository.findByCompanyIdAndPeriodId(companyId, periodId);
        Map<UUID, AvailabilitySubmission> subMap = submissions.stream()
                .collect(Collectors.toMap(
                        s -> s.getMember().getId(),
                        s -> s
                ));

        return targetWorkers.stream()
                .map(cm -> {
                    Member m = cm.getMember();
                    AvailabilitySubmission sub = subMap.get(m.getId());

                    boolean submitted = (sub != null);
                    LocalDateTime submittedAt = (sub != null) ? sub.getSubmittedAt() : null;

                    return AvailabilitySubmissionStatusResponse.builder()
                            .companyMemberId(cm.getId())
                            .memberId(m.getId())
                            .memberName(m.getName())
                            .submitted(submitted)
                            .submittedAt(submittedAt)
                            .build();
                }).toList();
    }


    ///  고정 근무자 dto 생성 관련 로직
    private FixedShiftResponse buildFixedShiftResponse(CompanyMember cm, boolean fixed, List<FixedShiftItemResponse> items) {
        return FixedShiftResponse.builder()
                .companyMemberId(cm.getId())
                .memberId(cm.getMember().getId())
                .memberName(cm.getMember().getName())
                .fixedShiftWorker(fixed)
                .shifts(items)
                .build();
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
        if(company.getCompanyMembers().stream().noneMatch(cm -> cm.getMember().getId().equals(memberId))) {
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
