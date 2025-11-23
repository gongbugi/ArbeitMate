package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.*;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import OpenSourceSW.ArbeitMate.dto.request.*;
import OpenSourceSW.ArbeitMate.dto.response.*;
import OpenSourceSW.ArbeitMate.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock CompanyRepository companyRepository;
    @Mock CompanyMemberRepository companyMemberRepository;
    @Mock CompanyRoleRepository companyRoleRepository;
    @Mock CompanyMemberRoleRepository companyMemberRoleRepository;
    @Mock ScheduleRepository scheduleRepository;
    @Mock SchedulePeriodRepository schedulePeriodRepository;
    @Mock StaffingTemplateRepository staffingTemplateRepository;
    @Mock FixedShiftRepository fixedShiftRepository;
    @Mock MemberAvailabilityRepository memberAvailabilityRepository;
    @Mock ScheduleSlotAvailabilityRepository scheduleSlotAvailabilityRepository;
    @Mock AvailabilitySubmissionRepository availabilitySubmissionRepository;

    @InjectMocks ScheduleService scheduleService;

    // ===== 테스트용 헬퍼 =====
    private Member newMember(String email, String name) {
        Member m = Member.create(email, name);
        return m;
    }

    private Company newCompany(String name, Member owner, String address, String inviteCode) {
        return Company.create(name, owner, address, inviteCode);
    }

    private CompanyRole newRole(Company company, String name) {
        return CompanyRole.create(company, name);
    }

    // =====================================================================
    // createWeeklyPeriod
    // =====================================================================
    @Test
    @DisplayName("커스텀 기간 생성 - 7일 이상이면 정상 생성")
    void createPeriod_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        LocalDate start = LocalDate.of(2025, 11, 17); // 월
        LocalDate end   = LocalDate.of(2025, 11, 23); // 일 (포함 7일)

        CreatePeriodRequest req = new CreatePeriodRequest();
        req.setName("11월 3주차");
        req.setStartDate(start);
        req.setEndDate(end);
        req.setAvailabilityDueAt(LocalDateTime.of(2025, 11, 16, 23, 0));

        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(false);

        // when
        SchedulePeriodResponse res =
                scheduleService.createPeriod(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo("11월 3주차");
        assertThat(res.getStartDate()).isEqualTo(start);
        assertThat(res.getEndDate()).isEqualTo(end);
        assertThat(res.getPeriodType()).isEqualTo(PeriodType.CUSTOM);

        verify(schedulePeriodRepository).existsOverlapping(companyId, start, end);
        verify(schedulePeriodRepository).save(any(SchedulePeriod.class));
    }

    @Test
    @DisplayName("커스텀 기간 생성 - 7일 미만이면 예외")
    void createPeriod_tooShort_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        LocalDate start = LocalDate.of(2025, 11, 17);
        LocalDate end   = LocalDate.of(2025, 11, 22); // 포함 6일

        CreatePeriodRequest req = new CreatePeriodRequest();
        req.setName("너무 짧은 기간");
        req.setStartDate(start);
        req.setEndDate(end);
        req.setAvailabilityDueAt(LocalDateTime.of(2025, 11, 16, 23, 0));

        // when & then
        assertThatThrownBy(() -> scheduleService.createPeriod(ownerId, companyId, req))
                .isInstanceOf(IllegalArgumentException.class);

        // 기간 검증에서 바로 걸리므로, 겹침 검증/저장은 호출되지 않아야 함
        verify(schedulePeriodRepository, never()).existsOverlapping(any(), any(), any());
        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("커스텀 기간 생성 - 종료일이 시작일보다 빠르면 예외")
    void createPeriod_endBeforeStart_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        LocalDate start = LocalDate.of(2025, 11, 23);
        LocalDate end   = LocalDate.of(2025, 11, 17);

        CreatePeriodRequest req = new CreatePeriodRequest();
        req.setName("역전된 기간");
        req.setStartDate(start);
        req.setEndDate(end);
        req.setAvailabilityDueAt(LocalDateTime.of(2025, 11, 16, 23, 0));

        // when & then
        assertThatThrownBy(() -> scheduleService.createPeriod(ownerId, companyId, req))
                .isInstanceOf(IllegalArgumentException.class);

        verify(schedulePeriodRepository, never()).existsOverlapping(any(), any(), any());
        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("커스텀 기간 생성 - 기존 기간과 겹치면 예외")
    void createPeriod_overlapping_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        LocalDate start = LocalDate.of(2025, 11, 17);
        LocalDate end   = LocalDate.of(2025, 11, 23); // 7일 이상

        CreatePeriodRequest req = new CreatePeriodRequest();
        req.setName("겹치는 기간");
        req.setStartDate(start);
        req.setEndDate(end);
        req.setAvailabilityDueAt(LocalDateTime.of(2025, 11, 16, 23, 0));

        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createPeriod(ownerId, companyId, req)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 다른 스케줄 기간이 존재");

        verify(schedulePeriodRepository).existsOverlapping(companyId, start, end);
        verify(schedulePeriodRepository, never()).save(any());
    }


    // =====================================================================
    // createWeeklyPeriod
    // =====================================================================
    @Test
    @DisplayName("주간 스케쥴 기간을 사장이 생성하면, 시작일+6일까지 DRAFT 상태로 생성된다")
    void createWeeklyPeriod_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate baseDate = LocalDate.of(2025, 11, 17); // 월요일이라고 가정
        LocalDate expectedStart = baseDate;
        LocalDate expectedEnd = baseDate.plusDays(6);

        CreateWeeklyPeriodRequest req = new CreateWeeklyPeriodRequest();
        req.setBaseDate(baseDate);
        req.setName("2025년 46주차");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, expectedStart, expectedEnd))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, "2025년 46주차"))
                .thenReturn(false);

        // when
        SchedulePeriodResponse res = scheduleService.createWeeklyPeriod(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo("2025년 46주차");
        assertThat(res.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
        assertThat(res.getStartDate()).isEqualTo(expectedStart);
        assertThat(res.getEndDate()).isEqualTo(expectedEnd);

        verify(schedulePeriodRepository, times(1)).save(any(SchedulePeriod.class));
    }

    @Test
    @DisplayName("주간 스케쥴 기간 생성 시 이름을 전달하지 않으면, 연도-주차(yyyy-Www) 형식으로 자동 생성된다")
    void createWeeklyPeriod_autoName_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate baseDate = LocalDate.of(2025, 11, 17);
        LocalDate expectedStart = baseDate;
        LocalDate expectedEnd = baseDate.plusDays(6);

        // 기대 이름 계산 (서비스와 동일한 WeekFields 사용)
        WeekFields wf = WeekFields.of(Locale.KOREA);
        int week = baseDate.get(wf.weekOfWeekBasedYear());
        String expectedName = baseDate.getYear() + "-W" + week;

        CreateWeeklyPeriodRequest req = new CreateWeeklyPeriodRequest();
        req.setBaseDate(baseDate);
        req.setName(null); // 자동 생성 경로
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, expectedStart, expectedEnd))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, expectedName))
                .thenReturn(false);

        // when
        SchedulePeriodResponse res = scheduleService.createWeeklyPeriod(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo(expectedName);
        assertThat(res.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
        assertThat(res.getStartDate()).isEqualTo(expectedStart);
        assertThat(res.getEndDate()).isEqualTo(expectedEnd);
    }

    @Test
    @DisplayName("주간 스케쥴 기간 생성 시, 같은 회사에 같은 이름의 기간이 이미 존재하면 예외 발생")
    void createWeeklyPeriod_duplicateName_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate baseDate = LocalDate.of(2025, 11, 17);
        LocalDate start = baseDate;
        LocalDate end = baseDate.plusDays(6);

        CreateWeeklyPeriodRequest req = new CreateWeeklyPeriodRequest();
        req.setBaseDate(baseDate);
        req.setName("중복이름");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, "중복이름"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createWeeklyPeriod(ownerId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("주간 스케쥴 기간 생성 시, 이미 다른 기간과 날짜가 겹치면 예외 발생")
    void createWeeklyPeriod_overlap_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate baseDate = LocalDate.of(2025, 11, 17);
        LocalDate start = baseDate;
        LocalDate end = baseDate.plusDays(6);

        CreateWeeklyPeriodRequest req = new CreateWeeklyPeriodRequest();
        req.setBaseDate(baseDate);
        req.setName("겹치는 기간");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createWeeklyPeriod(ownerId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("주간 스케쥴 기간 생성 시, 요청자가 사장이 아니면 예외 발생")
    void createWeeklyPeriod_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate baseDate = LocalDate.of(2025, 11, 17);

        CreateWeeklyPeriodRequest req = new CreateWeeklyPeriodRequest();
        req.setBaseDate(baseDate);
        req.setName("주간기간");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createWeeklyPeriod(otherMemberId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    // =====================================================================
    // createMonthlyPeriod
    // =====================================================================
    @Test
    @DisplayName("월간 스케쥴 기간을 사장이 생성하면, 해당 월의 1일부터 말일까지 DRAFT 상태로 생성된다")
    void createMonthlyPeriod_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        int year = 2025;
        int month = 11;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        CreateMonthlyPeriodRequest req = new CreateMonthlyPeriodRequest();
        req.setYear(year);
        req.setMonth(month);
        req.setName("2025-11 스케줄");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, "2025-11 스케줄"))
                .thenReturn(false);

        // when
        SchedulePeriodResponse res = scheduleService.createMonthlyPeriod(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo("2025-11 스케줄");
        assertThat(res.getPeriodType()).isEqualTo(PeriodType.MONTHLY);
        assertThat(res.getStartDate()).isEqualTo(start);
        assertThat(res.getEndDate()).isEqualTo(end);

        verify(schedulePeriodRepository, times(1)).save(any(SchedulePeriod.class));
    }

    @Test
    @DisplayName("월간 스케쥴 기간 생성 시 이름을 전달하지 않으면 yyyy-MM 형식으로 자동 생성된다")
    void createMonthlyPeriod_autoName_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        int year = 2025;
        int month = 11;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        String expectedName = String.format("%04d-%02d", year, month); // 2025-11

        CreateMonthlyPeriodRequest req = new CreateMonthlyPeriodRequest();
        req.setYear(year);
        req.setMonth(month);
        req.setName(null); // 자동 생성 경로
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, expectedName))
                .thenReturn(false);

        // when
        SchedulePeriodResponse res = scheduleService.createMonthlyPeriod(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo(expectedName);
        assertThat(res.getPeriodType()).isEqualTo(PeriodType.MONTHLY);
        assertThat(res.getStartDate()).isEqualTo(start);
        assertThat(res.getEndDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("월간 스케쥴 기간 생성 시, 같은 회사에 같은 이름의 기간이 이미 존재하면 예외 발생")
    void createMonthlyPeriod_duplicateName_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        int year = 2025;
        int month = 11;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        CreateMonthlyPeriodRequest req = new CreateMonthlyPeriodRequest();
        req.setYear(year);
        req.setMonth(month);
        req.setName("중복");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(false);
        when(schedulePeriodRepository.existsByCompanyIdAndName(companyId, "중복"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createMonthlyPeriod(ownerId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("월간 스케쥴 기간 생성 시, 이미 다른 기간과 날짜가 겹치면 예외 발생")
    void createMonthlyPeriod_overlap_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        int year = 2025;
        int month = 11;
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        CreateMonthlyPeriodRequest req = new CreateMonthlyPeriodRequest();
        req.setYear(year);
        req.setMonth(month);
        req.setName("겹침");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.existsOverlapping(companyId, start, end))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createMonthlyPeriod(ownerId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    @Test
    @DisplayName("월간 스케쥴 기간 생성 시, 요청자가 사장이 아니면 예외 발생")
    void createMonthlyPeriod_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CreateMonthlyPeriodRequest req = new CreateMonthlyPeriodRequest();
        req.setYear(2025);
        req.setMonth(11);
        req.setName("월간");
        req.setAvailabilityDueAt(LocalDateTime.now().plusDays(1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createMonthlyPeriod(otherMemberId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).save(any());
    }

    // =====================================================================
    // createSlots
    // =====================================================================
    @Test
    @DisplayName("DRAFT 상태의 기간 내에서, 사장은 날짜별 시간대별 슬롯을 여러 개 생성할 수 있다")
    void createSlots_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        // 기간: 2025-11-17 ~ 2025-11-23
        LocalDate start = LocalDate.of(2025, 11, 17);
        LocalDate end = start.plusDays(6);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                start,
                end,
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);
        // 생성 시 status = DRAFT 이므로 그대로 사용

        // 역할
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        // 슬롯 요청 2개
        CreateScheduleSlotRequest s1 = new CreateScheduleSlotRequest();
        s1.setRoleId(roleId);
        s1.setWorkDate(start); // 기간 내
        s1.setStartTime(LocalTime.of(10, 0));
        s1.setEndTime(LocalTime.of(14, 0));
        s1.setRequiredHeadCount(2);

        CreateScheduleSlotRequest s2 = new CreateScheduleSlotRequest();
        s2.setRoleId(roleId);
        s2.setWorkDate(start.plusDays(1)); // 기간 내
        s2.setStartTime(LocalTime.of(14, 0));
        s2.setEndTime(LocalTime.of(18, 0));
        s2.setRequiredHeadCount(1);

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of(s1, s2));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyRoleRepository.findById(roleId)).thenReturn(Optional.of(role));
        // saveAll 은 전달받은 리스트를 그대로 반환하도록 설정
        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<ScheduleSlotResponse> res =
                scheduleService.createSlots(ownerId, companyId, periodId, req);

        // then
        assertThat(res).hasSize(2);
        assertThat(res)
                .extracting("workDate")
                .containsExactlyInAnyOrder(start, start.plusDays(1));
        assertThat(res)
                .extracting("requiredHeadCount")
                .containsExactlyInAnyOrder(2, 1);

        verify(scheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 요청자가 사장이 아니면 예외 발생")
    void createSlots_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID periodId = UUID.randomUUID(); // 실제 조회는 필요 없음, 예외가 그 전에 발생

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of()); // 내용은 중요하지 않음

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(otherMemberId, companyId, periodId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).findById(any());
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 기간 ID가 존재하지 않으면 예외 발생")
    void createSlots_periodNotFound_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID periodId = UUID.randomUUID();

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of());

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(ownerId, companyId, periodId, req)
        ).isInstanceOf(IllegalArgumentException.class);

        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 기간이 다른 매장에 속한 경우 예외 발생")
    void createSlots_periodCompanyMismatch_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        // 매장 1 (owner)
        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        // 매장 2
        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        SchedulePeriod period = SchedulePeriod.create(
                company2,
                "다른매장기간",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of());

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(ownerId, companyId1, periodId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 기간 상태가 DRAFT가 아니면 예외 발생")
    void createSlots_periodNotDraft_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);
        // 상태를 PUBLISHED 로 강제 변경 후 테스트
        ReflectionTestUtils.setField(period, "status", PeriodStatus.PUBLISHED);

        CreateScheduleSlotRequest s1 = new CreateScheduleSlotRequest();
        s1.setWorkDate(LocalDate.of(2025, 11, 17));
        s1.setStartTime(LocalTime.of(10, 0));
        s1.setEndTime(LocalTime.of(14, 0));
        s1.setRequiredHeadCount(1);
        s1.setRoleId(UUID.randomUUID());

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of(s1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(ownerId, companyId, periodId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 기간 범위를 벗어난 날짜가 하나라도 포함되면 예외 발생")
    void createSlots_outOfRangeDate_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate start = LocalDate.of(2025, 11, 17);
        LocalDate end = start.plusDays(6);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                start,
                end,
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        // 기간 밖 날짜(예: end + 1)
        CreateScheduleSlotRequest s1 = new CreateScheduleSlotRequest();
        s1.setRoleId(roleId);
        s1.setWorkDate(end.plusDays(1)); // 범위 밖
        s1.setStartTime(LocalTime.of(10, 0));
        s1.setEndTime(LocalTime.of(14, 0));
        s1.setRequiredHeadCount(1);

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of(s1));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(ownerId, companyId, periodId, req)
        ).isInstanceOf(IllegalArgumentException.class);

        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("슬롯 생성 시, 다른 매장의 역할군으로 슬롯을 생성하려 하면 예외 발생")
    void createSlots_roleNotFromCompany_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        // 매장 1
        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        // 매장 2
        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        LocalDate start = LocalDate.of(2025, 11, 17);
        LocalDate end = start.plusDays(6);

        SchedulePeriod period = SchedulePeriod.create(
                company1,
                "2025-W47",
                PeriodType.WEEKLY,
                start,
                end,
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        // role 은 company2 소속
        CompanyRole otherRole = newRole(company2, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(otherRole, "id", roleId);

        CreateScheduleSlotRequest s1 = new CreateScheduleSlotRequest();
        s1.setRoleId(roleId);
        s1.setWorkDate(start);
        s1.setStartTime(LocalTime.of(10, 0));
        s1.setEndTime(LocalTime.of(14, 0));
        s1.setRequiredHeadCount(1);

        CreateScheduleSlotsRequest req = new CreateScheduleSlotsRequest();
        req.setSlots(List.of(s1));

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyRoleRepository.findById(roleId)).thenReturn(Optional.of(otherRole));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createSlots(ownerId, companyId1, periodId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleRepository, never()).saveAll(anyList());
    }

    // =====================================================================
    // 템플릿 관련 추가 기능 테스트
    // =====================================================================
    @Test
    @DisplayName("사장은 템플릿 이름과 항목들을 넘겨서 근무 템플릿을 생성할 수 있다")
    void createStaffingTemplate_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyRole role1 = newRole(company, "홀");
        UUID roleId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(role1, "id", roleId1);

        CompanyRole role2 = newRole(company, "주방");
        UUID roleId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(role2, "id", roleId2);

        CreateStaffingTemplateItemRequest i1 = new CreateStaffingTemplateItemRequest();
        i1.setDow(1); // 월요일
        i1.setRoleId(roleId1);
        i1.setStartTime(LocalTime.of(10, 0));
        i1.setEndTime(LocalTime.of(14, 0));
        i1.setRequiredHeadCount(2);

        CreateStaffingTemplateItemRequest i2 = new CreateStaffingTemplateItemRequest();
        i2.setDow(5); // 금요일
        i2.setRoleId(roleId2);
        i2.setStartTime(LocalTime.of(18, 0));
        i2.setEndTime(LocalTime.of(22, 0));
        i2.setRequiredHeadCount(1);

        CreateStaffingTemplateRequest req = new CreateStaffingTemplateRequest();
        req.setName("주간 기본 템플릿");
        req.setItems(List.of(i1, i2));

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRoleRepository.findById(roleId1)).thenReturn(Optional.of(role1));
        when(companyRoleRepository.findById(roleId2)).thenReturn(Optional.of(role2));
        when(staffingTemplateRepository.save(any(StaffingTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        StaffingTemplateResponse res =
                scheduleService.createStaffingTemplate(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo("주간 기본 템플릿");
        assertThat(res.getItems()).hasSize(2);

        verify(staffingTemplateRepository, times(1)).save(any(StaffingTemplate.class));
    }

    @Test
    @DisplayName("사장이 아닌 회원은 템플릿을 생성할 수 없다")
    void createStaffingTemplate_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CreateStaffingTemplateRequest req = new CreateStaffingTemplateRequest();
        req.setName("템플릿");
        req.setItems(List.of());

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createStaffingTemplate(otherMemberId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(staffingTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 매장의 역할군을 포함한 템플릿을 생성하려 하면 예외 발생")
    void createStaffingTemplate_roleNotFromCompany_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        CompanyRole roleOther = newRole(company2, "홀");
        UUID roleOtherId = UUID.randomUUID();
        ReflectionTestUtils.setField(roleOther, "id", roleOtherId);

        CreateStaffingTemplateItemRequest item = new CreateStaffingTemplateItemRequest();
        item.setDow(1);
        item.setRoleId(roleOtherId);
        item.setStartTime(LocalTime.of(10, 0));
        item.setEndTime(LocalTime.of(14, 0));
        item.setRequiredHeadCount(1);

        CreateStaffingTemplateRequest req = new CreateStaffingTemplateRequest();
        req.setName("잘못된 템플릿");
        req.setItems(List.of(item));

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(companyRoleRepository.findById(roleOtherId)).thenReturn(Optional.of(roleOther));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createStaffingTemplate(ownerId, companyId1, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(staffingTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("사장은 회사의 템플릿 목록을 조회할 수 있다")
    void listStaffingTemplates_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        StaffingTemplate t1 = StaffingTemplate.create(company, "템플릿1", owner);
        StaffingTemplate t2 = StaffingTemplate.create(company, "템플릿2", owner);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(staffingTemplateRepository.findByCompanyId(companyId))
                .thenReturn(List.of(t1, t2));

        // when
        List<StaffingTemplateResponse> res =
                scheduleService.listStaffingTemplates(ownerId, companyId);

        // then
        assertThat(res).hasSize(2);
        assertThat(res)
                .extracting("name")
                .containsExactlyInAnyOrder("템플릿1", "템플릿2");
    }

    @Test
    @DisplayName("사장이 아닌 회원은 템플릿 목록을 조회할 수 없다")
    void listStaffingTemplates_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.listStaffingTemplates(otherMemberId, companyId)
        ).isInstanceOf(IllegalStateException.class);

        verify(staffingTemplateRepository, never()).findByCompanyId(any());
    }

    @Test
    @DisplayName("저장된 템플릿으로 기간 내 요일에 맞는 슬롯을 생성하면, 기존 슬롯을 모두 삭제한 뒤 새로 생성된다")
    void applyTemplateToPeriod_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        LocalDate start = LocalDate.of(2025, 11, 17); // MONDAY
        LocalDate end = start.plusDays(6);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                start,
                end,
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        // 템플릿: 월요일 10~14시, 홀 2명
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        StaffingTemplate template = StaffingTemplate.create(company, "주간 기본", owner);
        UUID templateId = UUID.randomUUID();
        ReflectionTestUtils.setField(template, "id", templateId);

        StaffingTemplateItem item = StaffingTemplateItem.create(
                template,
                role,
                0,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                2
        );

        // 기존에 이 period에 이미 깔려 있던 스케줄(삭제 대상)
        Schedule existingSchedule = Schedule.create(
                company,
                period,
                role,
                start,                       // 어떤 날짜든 상관 없음
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                1
        );
        List<Schedule> existing = List.of(existingSchedule);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(staffingTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        when(scheduleRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<ScheduleSlotResponse> res =
                scheduleService.applyTemplateToPeriod(ownerId, companyId, periodId, templateId);

        // then
        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getWorkDate()).isEqualTo(start);
        assertThat(res.getFirst().getRequiredHeadCount()).isEqualTo(2);

        verify(scheduleRepository, times(1)).deleteByPeriodId(any());
        verify(scheduleRepository, times(1)).saveAll(anyList());
    }


    @Test
    @DisplayName("템플릿 적용 시, 요청자가 사장이 아니면 예외 발생")
    void applyTemplateToPeriod_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID periodId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.applyTemplateToPeriod(otherMemberId, companyId, periodId, templateId)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).findById(any());
        verify(staffingTemplateRepository, never()).findById(any());
        verify(scheduleRepository, never()).deleteAll(any());
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("템플릿 적용 시, 템플릿이 다른 매장에 속한 경우 예외 발생")
    void applyTemplateToPeriod_templateCompanyMismatch_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        SchedulePeriod period = SchedulePeriod.create(
                company1,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        StaffingTemplate template = StaffingTemplate.create(company2, "다른매장템플릿", owner);
        UUID templateId = UUID.randomUUID();
        ReflectionTestUtils.setField(template, "id", templateId);

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(staffingTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.applyTemplateToPeriod(ownerId, companyId1, periodId, templateId)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleRepository, never()).deleteAll(any());
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("템플릿 적용 시, 기간 상태가 DRAFT가 아니면 예외 발생")
    void applyTemplateToPeriod_periodNotDraft_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);
        ReflectionTestUtils.setField(period, "status", PeriodStatus.PUBLISHED); // DRAFT 아님

        UUID templateId = UUID.randomUUID(); // 실제로는 조회까지 가지 않음

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.applyTemplateToPeriod(ownerId, companyId, periodId, templateId)
        ).isInstanceOf(IllegalStateException.class);

        // 템플릿/스케줄 관련 리포지토리는 전혀 호출되지 않아야 한다
        verify(staffingTemplateRepository, never()).findById(any());
        verify(scheduleRepository, never()).deleteAll(any());
        verify(scheduleRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("기존 SchedulePeriod에 이미 생성된 스케쥴들을 기반으로 템플릿을 자동 생성할 수 있다")
    void createTemplateFromPeriod_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        // --- role 생성 + ID 세팅 ---
        CompanyRole role1 = newRole(company, "홀");
        UUID role1Id = UUID.randomUUID();
        ReflectionTestUtils.setField(role1, "id", role1Id);

        CompanyRole role2 = newRole(company, "주방");
        UUID role2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(role2, "id", role2Id);

        // --- 스케줄 생성 ---
        Schedule s1 = Schedule.create(
                company,
                period,
                role1,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                2
        );
        Schedule s2 = Schedule.create(
                company,
                period,
                role2,
                LocalDate.of(2025, 11, 18),
                LocalTime.of(18, 0),
                LocalTime.of(22, 0),
                1
        );

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(scheduleRepository.findByPeriod(period)).thenReturn(List.of(s1, s2));

        // 서비스에서 role을 다시 조회하므로 이 stubbing이 필요함
        when(companyRoleRepository.findById(role1Id)).thenReturn(Optional.of(role1));
        when(companyRoleRepository.findById(role2Id)).thenReturn(Optional.of(role2));

        when(staffingTemplateRepository.save(any(StaffingTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        StaffingTemplateResponse res =
                scheduleService.createTemplateFromPeriod(ownerId, companyId, periodId);

        // then
        assertThat(res.getName()).isEqualTo("W47-TEMPLATE");
        assertThat(res.getItems()).hasSize(2);

        verify(staffingTemplateRepository, times(1)).save(any(StaffingTemplate.class));
    }

    @Test
    @DisplayName("기존 기간 기반 템플릿 자동 생성 시, 사장이 아니면 예외 발생")
    void createTemplateFromPeriod_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID periodId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createTemplateFromPeriod(otherMemberId, companyId, periodId)
        ).isInstanceOf(IllegalStateException.class);

        verify(schedulePeriodRepository, never()).findById(any());
        verify(scheduleRepository, never()).findByPeriod(any());
        verify(staffingTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("기존 기간 기반 템플릿 자동 생성 시, 기간이 다른 매장에 속하면 예외 발생")
    void createTemplateFromPeriod_periodCompanyMismatch_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        SchedulePeriod period = SchedulePeriod.create(
                company2,
                "다른매장기간",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createTemplateFromPeriod(ownerId, companyId1, periodId)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleRepository, never()).findByPeriod(any());
        verify(staffingTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("기존 기간에 스케쥴이 하나도 없으면 템플릿 자동 생성 시 예외 발생")
    void createTemplateFromPeriod_noSchedules_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(scheduleRepository.findByPeriod(period)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() ->
                scheduleService.createTemplateFromPeriod(ownerId, companyId, periodId)
        ).isInstanceOf(IllegalStateException.class);

        verify(staffingTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("사장은 자신의 매장의 템플릿을 삭제할 수 있다")
    void deleteTemplate_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        StaffingTemplate template = StaffingTemplate.create(company, "주간 기본", owner);
        UUID templateId = UUID.randomUUID();
        ReflectionTestUtils.setField(template, "id", templateId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(staffingTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // when
        scheduleService.deleteTemplate(ownerId, companyId, templateId);

        // then
        verify(staffingTemplateRepository, times(1)).delete(template);
    }

    @Test
    @DisplayName("사장이 아닌 회원이 템플릿 삭제 시 예외 발생")
    void deleteTemplate_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID templateId = UUID.randomUUID(); // 템플릿 조회까지 가지 않게 할 것

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.deleteTemplate(otherMemberId, companyId, templateId)
        ).isInstanceOf(IllegalStateException.class);

        // 템플릿 리포지토리는 건드리지 않아야 함
        verify(staffingTemplateRepository, never()).findById(any());
        verify(staffingTemplateRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 매장의 템플릿을 삭제하려 하면 예외 발생")
    void deleteTemplate_templateCompanyMismatch_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        // 매장 1 (요청 기준 매장)
        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        // 매장 2 (템플릿이 실제로 속한 매장)
        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        StaffingTemplate template = StaffingTemplate.create(company2, "다른 매장 템플릿", owner);
        UUID templateId = UUID.randomUUID();
        ReflectionTestUtils.setField(template, "id", templateId);

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(staffingTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.deleteTemplate(ownerId, companyId1, templateId)
        ).isInstanceOf(IllegalStateException.class);

        verify(staffingTemplateRepository, never()).delete(any());
    }

    // =====================================================================
    // updateMemberAvailabilityPattern
    // =====================================================================
    @Test
    @DisplayName("희망 근무 시간 등록 시 - 기존 패턴 전체 삭제 후 새 패턴 저장")
    void updateMemberAvailabilityPattern_success() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member member = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(member, "id", memberId);

        Company company = newCompany("카페 A", member, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember cm = mock(CompanyMember.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(member);

        MemberAvailabilityItemRequest i1 = new MemberAvailabilityItemRequest();
        i1.setDow(0);
        i1.setStartTime(LocalTime.of(10, 0));
        i1.setEndTime(LocalTime.of(14, 0));
        i1.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        i1.setEffectiveTo(LocalDate.of(2025, 12, 31));

        MemberAvailabilityItemRequest i2 = new MemberAvailabilityItemRequest();
        i2.setDow(3);
        i2.setStartTime(LocalTime.of(18, 0));
        i2.setEndTime(LocalTime.of(22, 0));
        i2.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        i2.setEffectiveTo(LocalDate.of(2025, 12, 31));

        UpdateMemberAvailabilityRequest req = new UpdateMemberAvailabilityRequest();
        req.setItems(List.of(i1, i2));

        // when
        scheduleService.updateMemberAvailabilityPattern(memberId, companyId, req);

        // then
        verify(memberAvailabilityRepository, times(1))
                .deleteByCompanyIdAndMemberId(companyId, memberId);
        verify(memberAvailabilityRepository, times(1))
                .saveAll(anyList());
    }

    @Test
    @DisplayName("희망 근무 시간 등록 시 - 고정 근무자는 등록 불가")
    void updateMemberAvailabilityPattern_fixedWorker_throws() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member member = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(member, "id", memberId);

        Company company = newCompany("카페 A", member, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember cm = mock(CompanyMember.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));
        when(cm.isFixedShiftWorker()).thenReturn(true);

        UpdateMemberAvailabilityRequest req = new UpdateMemberAvailabilityRequest();
        req.setItems(List.of()); // 내용 무관

        // when & then
        assertThatThrownBy(() ->
                scheduleService.updateMemberAvailabilityPattern(memberId, companyId, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(memberAvailabilityRepository, never()).saveAll(anyList());
    }

    // =====================================================================
    // getMemberAvailabilityPattern
    // =====================================================================
    @Test
    @DisplayName("희망 근무 시간 조회 - 정상 조회")
    void getMemberAvailabilityPattern_success() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member member = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(member, "id", memberId);

        Company company = newCompany("카페 A", member, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember cm = mock(CompanyMember.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(member);

        MemberAvailability a1 = MemberAvailability.create(
                company, member, 0,
                LocalTime.of(10, 0), LocalTime.of(14, 0),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)
        );
        MemberAvailability a2 = MemberAvailability.create(
                company, member, 3,
                LocalTime.of(18, 0), LocalTime.of(22, 0),
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)
        );

        when(memberAvailabilityRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(a1, a2));

        // when
        MemberAvailabilityResponse res =
                scheduleService.getMemberAvailabilityPattern(memberId, companyId);

        // then
        assertThat(res.getMemberId()).isEqualTo(memberId);
        assertThat(res.getCompanyId()).isEqualTo(companyId);
        assertThat(res.getMemberName()).isEqualTo(member.getName());
        assertThat(res.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("희망 근무 시간 조회 - 고정 근무자는 예외 발생")
    void getMemberAvailabilityPattern_fixedWorker_throws() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member member = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(member, "id", memberId);

        Company company = newCompany("카페 A", member, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember cm = mock(CompanyMember.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));
        when(cm.isFixedShiftWorker()).thenReturn(true);

        // when & then
        assertThatThrownBy(() ->
                scheduleService.getMemberAvailabilityPattern(memberId, companyId)
        ).isInstanceOf(IllegalStateException.class);

        verify(memberAvailabilityRepository, never()).findByCompanyIdAndMemberId(any(), any());
    }

    // =====================================================================
    // updateFixedShift
    // =====================================================================
    @Test
    @DisplayName("고정 근무자 해제 - 플래그 해제 후 패턴 전체 삭제")
    void updateFixedShift_unmark_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID memberId = UUID.randomUUID();
        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        UUID companyMemberId = UUID.randomUUID();
        CompanyMember cm = mock(CompanyMember.class);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findById(companyMemberId)).thenReturn(Optional.of(cm));
        when(cm.getCompany()).thenReturn(company);
        when(cm.getMember()).thenReturn(worker);

        UpdateFixedShiftRequest req = new UpdateFixedShiftRequest();
        req.setFixedShiftWorker(false);
        req.setShifts(List.of());

        // when
        FixedShiftResponse res =
                scheduleService.updateFixedShift(ownerId, companyId, companyMemberId, req);

        // then
        verify(cm, times(1)).unmarkAsFixedShiftWorker();
        verify(fixedShiftRepository, times(1))
                .deleteByCompanyIdAndMemberId(companyId, memberId);
        verify(fixedShiftRepository, never()).saveAll(anyList());

        assertThat(res.isFixedShiftWorker()).isFalse();
        assertThat(res.getShifts()).isEmpty();
    }

    @Test
    @DisplayName("고정 근무자 등록/갱신 - 플래그 ON + 기존 삭제 후 새 패턴 저장")
    void updateFixedShift_mark_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        UUID memberId = UUID.randomUUID();
        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        UUID companyMemberId = UUID.randomUUID();
        CompanyMember cm = mock(CompanyMember.class);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findById(companyMemberId)).thenReturn(Optional.of(cm));
        when(cm.getCompany()).thenReturn(company);
        when(cm.getMember()).thenReturn(worker);

        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);
        when(companyRoleRepository.findById(roleId)).thenReturn(Optional.of(role));

        FixedShiftItemRequest item = new FixedShiftItemRequest();
        item.setRoleId(roleId);
        item.setDow(0);
        item.setStartTime(LocalTime.of(10, 0));
        item.setEndTime(LocalTime.of(14, 0));
        item.setEffectiveFrom(LocalDate.of(2025, 1, 1));
        item.setEffectiveTo(LocalDate.of(2025, 12, 31));

        UpdateFixedShiftRequest req = new UpdateFixedShiftRequest();
        req.setFixedShiftWorker(true);
        req.setShifts(List.of(item));

        // when
        FixedShiftResponse res =
                scheduleService.updateFixedShift(ownerId, companyId, companyMemberId, req);

        // then
        verify(cm, times(1)).markAsFixedShiftWorker();
        verify(fixedShiftRepository, times(1))
                .deleteByCompanyIdAndMemberId(companyId, memberId);
        verify(fixedShiftRepository, times(1)).saveAll(anyList());

        assertThat(res.isFixedShiftWorker()).isTrue();
        assertThat(res.getShifts()).hasSize(1);
    }

    // =====================================================================
    // getAllFixedShiftConfig
    // =====================================================================
    @Test
    @DisplayName("전체 고정 근무자 조회 - WORKER 중 fixed=true 인 멤버만 반환")
    void getAllFixedShiftConfig_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        Member m1 = newMember("w1@test.com", "알바1");
        UUID m1Id = UUID.randomUUID();
        ReflectionTestUtils.setField(m1, "id", m1Id);

        Member m2 = newMember("w2@test.com", "알바2");
        UUID m2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(m2, "id", m2Id);

        CompanyMember cm1 = mock(CompanyMember.class);
        CompanyMember cm2 = mock(CompanyMember.class);

        when(cm1.isFixedShiftWorker()).thenReturn(true);
        when(cm2.isFixedShiftWorker()).thenReturn(false);

        UUID cm1Id = UUID.randomUUID();
        when(cm1.getId()).thenReturn(cm1Id);
        when(cm1.getMember()).thenReturn(m1);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndRole(companyId, MembershipRole.WORKER))
                .thenReturn(List.of(cm1, cm2));

        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        FixedShift fs1 = FixedShift.create(
                company, m1, role,
                0,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        FixedShift fs2 = FixedShift.create(
                company, m2, role,
                0,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        when(fixedShiftRepository.findByCompanyId(companyId))
                .thenReturn(List.of(fs1, fs2));

        // when
        List<FixedShiftResponse> res =
                scheduleService.getAllFixedShiftConfig(ownerId, companyId);

        // then
        assertThat(res).hasSize(1);
        FixedShiftResponse r1 = res.getFirst();
        assertThat(r1.getMemberId()).isEqualTo(m1Id);
        assertThat(r1.getShifts()).hasSize(1);
    }

    @Test
    @DisplayName("전체 고정 근무자 조회 - 고정 근무자가 없으면 빈 리스트")
    void getAllFixedShiftConfig_noFixed_returnEmpty() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findByCompanyIdAndRole(companyId, MembershipRole.WORKER))
                .thenReturn(List.of(cm));

        // when
        List<FixedShiftResponse> res =
                scheduleService.getAllFixedShiftConfig(ownerId, companyId);

        // then
        assertThat(res).isEmpty();
        verify(fixedShiftRepository, never()).findByCompanyId(any());
    }

    // =====================================================================
    // getFixedShiftConfig
    // =====================================================================
    @Test
    @DisplayName("특정 고정 근무자 패턴 조회 - 정상 조회")
    void getFixedShiftConfig_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        Member worker = newMember("worker@test.com", "알바");
        UUID memberId = UUID.randomUUID();
        ReflectionTestUtils.setField(worker, "id", memberId);

        UUID companyMemberId = UUID.randomUUID();
        CompanyMember cm = mock(CompanyMember.class);
        when(cm.getCompany()).thenReturn(company);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMemberRepository.findById(companyMemberId)).thenReturn(Optional.of(cm));

        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        FixedShift fs1 = FixedShift.create(
                company, worker, role,
                0,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        when(fixedShiftRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(fs1));

        // when
        FixedShiftResponse res =
                scheduleService.getFixedShiftConfig(ownerId, companyId, companyMemberId);

        // then
        assertThat(res.getMemberId()).isEqualTo(memberId);
        assertThat(res.getShifts()).hasSize(1);
    }

    @Test
    @DisplayName("특정 고정 근무자 패턴 조회 - 다른 매장 직원이면 예외")
    void getFixedShiftConfig_companyMismatch_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company1 = newCompany("카페 A", owner, "서울", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        Company company2 = newCompany("카페 B", owner, "서울", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2); // ✅ 이 줄 추가

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.getCompany()).thenReturn(company2);

        UUID companyMemberId = UUID.randomUUID();

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(companyMemberRepository.findById(companyMemberId)).thenReturn(Optional.of(cm));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.getFixedShiftConfig(ownerId, companyId1, companyMemberId)
        ).isInstanceOf(IllegalStateException.class);

        verify(fixedShiftRepository, never()).findByCompanyIdAndMemberId(any(), any());
    }


    // =====================================================================
    // getWorkerAvailabilitySlots
    // =====================================================================
    @Test
    @DisplayName("근무자 가용 슬롯 조회 - 역할/패턴/이미 제출 내역을 반영해 recommended/willing 반환")
    void getWorkerAvailabilitySlots_success() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        // 회사 / 기간
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        // 멤버 & 회사멤버
        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));

        // 역할
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        CompanyMemberRole cmr = mock(CompanyMemberRole.class);
        when(cmr.getRole()).thenReturn(role);
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(cmr));

        // 슬롯 2개
        Schedule slot1 = Schedule.create(
                company,
                period,
                role,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                1
        );
        Schedule slot2 = Schedule.create(
                company,
                period,
                role,
                LocalDate.of(2025, 11, 18),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                1
        );
        UUID slotId1 = UUID.randomUUID();
        UUID slotId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(slot1, "id", slotId1);
        ReflectionTestUtils.setField(slot2, "id", slotId2);

        when(scheduleRepository.findByPeriod(period))
                .thenReturn(List.of(slot1, slot2));

        // 패턴 : 월/화 둘 다 겹치게
        MemberAvailability p1 = MemberAvailability.create(
                company,
                worker,
                0, // 월요일
                LocalTime.of(9, 0),
                LocalTime.of(23, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        MemberAvailability p2 = MemberAvailability.create(
                company,
                worker,
                1, // 화요일
                LocalTime.of(9, 0),
                LocalTime.of(23, 0),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        when(memberAvailabilityRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(p1, p2));

        // 이미 제출한 슬롯 : slot1만 willing
        ScheduleSlotAvailability a1 = ScheduleSlotAvailability.willing(slot1, worker);
        when(scheduleSlotAvailabilityRepository.findByMemberAndPeriod(memberId, period))
                .thenReturn(List.of(a1));

        // when
        WorkerAvailabilitySlotsResponse res =
                scheduleService.getWorkerAvailabilitySlots(memberId, companyId, periodId);

        // then
        assertThat(res.getRecommendedSlots()).hasSize(2);
        assertThat(res.getOtherSlots()).isEmpty();

        WorkerSlotResponse first = res.getRecommendedSlots().getFirst();
        assertThat(first.isWilling()).isTrue(); // slot1
    }

    @Test
    @DisplayName("근무자 가용 슬롯 조회 - 가능한 역할이 없으면 빈 리스트 반환")
    void getWorkerAvailabilitySlots_noRoles_returnEmpty() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of()); // 역할 없음

        // when
        WorkerAvailabilitySlotsResponse res =
                scheduleService.getWorkerAvailabilitySlots(memberId, companyId, periodId);

        // then
        assertThat(res.getRecommendedSlots()).isEmpty();
        assertThat(res.getOtherSlots()).isEmpty();

        verify(scheduleRepository, never()).findByPeriod(any());
    }

    // =====================================================================
    // submitAvailability
    // =====================================================================
    @Test
    @DisplayName("가용 시간 최초 제출 - 이전 내역 삭제 후 새로 저장 + 제출 기록 생성")
    void submitAvailability_firstTime_success() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company,
                "2025-W47",
                PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));

        // 역할
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        CompanyMemberRole cmr = mock(CompanyMemberRole.class);
        when(cmr.getRole()).thenReturn(role);
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(cmr));

        // 슬롯 2개
        Schedule s1 = Schedule.create(
                company, period, role,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0), LocalTime.of(14, 0),
                1
        );
        Schedule s2 = Schedule.create(
                company, period, role,
                LocalDate.of(2025, 11, 18),
                LocalTime.of(14, 0), LocalTime.of(18, 0),
                1
        );
        UUID s1Id = UUID.randomUUID();
        UUID s2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(s1, "id", s1Id);
        ReflectionTestUtils.setField(s2, "id", s2Id);

        when(scheduleRepository.findAllById(List.of(s1Id, s2Id)))
                .thenReturn(List.of(s1, s2));

        SubmitAvailabilityRequest req = new SubmitAvailabilityRequest();
        req.setSlotIds(List.of(s1Id, s2Id));

        when(availabilitySubmissionRepository
                .findByCompanyIdAndPeriodIdAndMemberId(companyId, periodId, memberId))
                .thenReturn(Optional.empty());

        // when
        scheduleService.submitAvailability(memberId, companyId, periodId, req);

        // then
        verify(scheduleSlotAvailabilityRepository, times(1))
                .deleteByMemberAndPeriod(memberId, period);
        verify(scheduleSlotAvailabilityRepository, times(1))
                .saveAll(anyList());
        verify(availabilitySubmissionRepository, times(1))
                .save(any(AvailabilitySubmission.class));
    }

    @Test
    @DisplayName("가용 시간 제출 시, 다른 기간의 슬롯이 포함되면 예외")
    void submitAvailability_otherPeriodSlot_throws() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period1 = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17), LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(period1, "id", periodId1);

        SchedulePeriod period2 = SchedulePeriod.create(
                company, "P2", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 24), LocalDate.of(2025, 11, 30),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(period2, "id", periodId2); // ✅ 이 줄 추가

        Member worker = newMember("worker@test.com", "알바");
        ReflectionTestUtils.setField(worker, "id", memberId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId1)).thenReturn(Optional.of(period1));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(Optional.of(cm));

        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);
        CompanyMemberRole cmr = mock(CompanyMemberRole.class);
        when(cmr.getRole()).thenReturn(role);
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, memberId))
                .thenReturn(List.of(cmr));

        // 다른 기간의 슬롯
        Schedule sOther = Schedule.create(
                company, period2, role,
                LocalDate.of(2025, 11, 25),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                1
        );
        UUID slotId = UUID.randomUUID();
        ReflectionTestUtils.setField(sOther, "id", slotId);
        when(scheduleRepository.findAllById(List.of(slotId)))
                .thenReturn(List.of(sOther));

        SubmitAvailabilityRequest req = new SubmitAvailabilityRequest();
        req.setSlotIds(List.of(slotId));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.submitAvailability(memberId, companyId, periodId1, req)
        ).isInstanceOf(IllegalStateException.class);

        verify(scheduleSlotAvailabilityRepository, never()).saveAll(anyList());
    }


    // =====================================================================
    // getAvailabilitySubmissionStatus
    // =====================================================================
    @Test
    @DisplayName("가용 제출 현황 조회 - 고정근무자 제외, 제출 여부/시간 포함")
    void getAvailabilitySubmissionStatus_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        // worker1 (제출 완료)
        Member m1 = newMember("w1@test.com", "알바1");
        UUID m1Id = UUID.randomUUID();
        ReflectionTestUtils.setField(m1, "id", m1Id);
        CompanyMember cm1 = mock(CompanyMember.class);
        when(cm1.getMember()).thenReturn(m1);
        when(cm1.getId()).thenReturn(UUID.randomUUID());
        when(cm1.isFixedShiftWorker()).thenReturn(false);

        // worker2 (미제출)
        Member m2 = newMember("w2@test.com", "알바2");
        UUID m2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(m2, "id", m2Id);
        CompanyMember cm2 = mock(CompanyMember.class);
        when(cm2.getMember()).thenReturn(m2);
        when(cm2.getId()).thenReturn(UUID.randomUUID());
        when(cm2.isFixedShiftWorker()).thenReturn(false);

        // worker3 (고정 근무자 - 목록에서 제외)
        Member m3 = newMember("w3@test.com", "알바3");
        UUID m3Id = UUID.randomUUID();
        ReflectionTestUtils.setField(m3, "id", m3Id);
        CompanyMember cm3 = mock(CompanyMember.class);
        when(cm3.isFixedShiftWorker()).thenReturn(true);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyMemberRepository.findByCompanyIdAndRole(companyId, MembershipRole.WORKER))
                .thenReturn(List.of(cm1, cm2, cm3));

        // 제출 기록: m1, m3만 있음 (m3는 어차피 제외)
        AvailabilitySubmission sub1 = AvailabilitySubmission.create(company, period, m1);
        ReflectionTestUtils.setField(sub1, "submittedAt", LocalDateTime.now().minusHours(1));

        AvailabilitySubmission sub3 = AvailabilitySubmission.create(company, period, m3);

        when(availabilitySubmissionRepository.findByCompanyIdAndPeriodId(companyId, periodId))
                .thenReturn(List.of(sub1, sub3));

        // when
        List<AvailabilitySubmissionStatusResponse> res =
                scheduleService.getAvailabilitySubmissionStatus(ownerId, companyId, periodId);

        // then
        assertThat(res).hasSize(2); // m1, m2
        var byId = res.stream().collect(Collectors.toMap(
                AvailabilitySubmissionStatusResponse::getMemberId,
                r -> r
        ));

        assertThat(byId.get(m1Id).isSubmitted()).isTrue();
        assertThat(byId.get(m2Id).isSubmitted()).isFalse();
        // m3Id 는 포함되지 않아야 함
        assertThat(byId.containsKey(m3Id)).isFalse();
    }

    // =====================================================================
    // autoAssignSchedules
    // =====================================================================
    @Test
    @DisplayName("자동 배치 - 고정 근무자 제외 일반 근무자의 가용 슬롯에 랜덤 배치")
    void autoAssignSchedules_basic_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        Member worker = newMember("w1@test.com", "알바1");
        UUID workerId = UUID.randomUUID();
        ReflectionTestUtils.setField(worker, "id", workerId);

        CompanyMember cm = mock(CompanyMember.class);
        when(cm.isFixedShiftWorker()).thenReturn(false);
        when(cm.getMember()).thenReturn(worker);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));
        when(companyMemberRepository.findByCompanyIdAndRole(companyId, MembershipRole.WORKER))
                .thenReturn(List.of(cm));

        // 고정 근무자는 없는 상황
        when(fixedShiftRepository.findActiveInPeriod(any(), any(), any()))
                .thenReturn(List.of());

        // 역할
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        CompanyMemberRole cmr = mock(CompanyMemberRole.class);
        when(cmr.getRole()).thenReturn(role);
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, workerId))
                .thenReturn(List.of(cmr));

        // 슬롯 1개
        Schedule slot = Schedule.create(
                company, period, role,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0), LocalTime.of(14, 0),
                1
        );
        UUID slotId = UUID.randomUUID();
        ReflectionTestUtils.setField(slot, "id", slotId);

        when(scheduleRepository.findByPeriod(period))
                .thenReturn(List.of(slot));

        // 가용 정보: worker가 slot에 willing
        ScheduleSlotAvailability avail = ScheduleSlotAvailability.willing(slot, worker);
        when(scheduleSlotAvailabilityRepository.findByPeriod(period))
                .thenReturn(List.of(avail));

        // when
        scheduleService.autoAssignSchedules(ownerId, companyId, periodId);

        // then : slot에 1명 배정되었는지 확인
        assertThat(slot.getAssignments())
                .hasSize(1)
                .first()
                .extracting(a -> a.getMember().getId())
                .isEqualTo(workerId);
    }

    // =====================================================================
    // updateScheduleAssignments
    // =====================================================================
    @Test
    @DisplayName("수동 편성 반영 - 요청된 근무자들로 각 슬롯 배정")
    void updateScheduleAssignments_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // 역할
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        // 슬롯
        Schedule slot = Schedule.create(
                company, period, role,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0), LocalTime.of(14, 0),
                2
        );
        UUID slotId = UUID.randomUUID();
        ReflectionTestUtils.setField(slot, "id", slotId);
        when(scheduleRepository.findByPeriod(period)).thenReturn(List.of(slot));

        // 근무자 2명
        Member w1 = newMember("w1@test.com", "알바1");
        UUID w1Id = UUID.randomUUID();
        ReflectionTestUtils.setField(w1, "id", w1Id);

        Member w2 = newMember("w2@test.com", "알바2");
        UUID w2Id = UUID.randomUUID();
        ReflectionTestUtils.setField(w2, "id", w2Id);

        CompanyMember cm1 = mock(CompanyMember.class);
        when(cm1.getRole()).thenReturn(MembershipRole.WORKER);
        when(cm1.isFixedShiftWorker()).thenReturn(false);
        when(cm1.getMember()).thenReturn(w1);

        CompanyMember cm2 = mock(CompanyMember.class);
        when(cm2.getRole()).thenReturn(MembershipRole.WORKER);
        when(cm2.isFixedShiftWorker()).thenReturn(false);
        when(cm2.getMember()).thenReturn(w2);

        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, w1Id))
                .thenReturn(Optional.of(cm1));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, w2Id))
                .thenReturn(Optional.of(cm2));

        // 역할 수행 가능 설정(canWorkRole 내부에서 조회)
        CompanyMemberRole cmr1 = mock(CompanyMemberRole.class);
        when(cmr1.getRole()).thenReturn(role);
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, w1Id))
                .thenReturn(List.of(cmr1));
        when(companyMemberRoleRepository.findByCompanyIdAndMemberId(companyId, w2Id))
                .thenReturn(List.of(cmr1));

        // 요청 DTO
        UpdateScheduleAssignmentsRequest.Item item = new UpdateScheduleAssignmentsRequest.Item();
        item.setScheduleId(slotId);
        item.setMemberIds(List.of(w1Id, w2Id));

        UpdateScheduleAssignmentsRequest req = new UpdateScheduleAssignmentsRequest();
        req.setItems(List.of(item));

        // when
        scheduleService.updateScheduleAssignments(ownerId, companyId, periodId, req);

        // then
        assertThat(slot.getAssignments()).hasSize(2);
        assertThat(slot.getAssignments())
                .extracting(a -> a.getMember().getId())
                .containsExactlyInAnyOrder(w1Id, w2Id);
    }

    @Test
    @DisplayName("수동 편성 반영 - 필요 인원보다 많이 배치하면 예외")
    void updateScheduleAssignments_tooManyMembers_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        // 기간(DRAFT)
        SchedulePeriod period = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // 역할 + 슬롯(정원 1명)
        CompanyRole role = newRole(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        Schedule slot = Schedule.create(
                company, period, role,
                LocalDate.of(2025, 11, 17),
                LocalTime.of(10, 0), LocalTime.of(14, 0),
                1   // 정원 1명
        );
        UUID slotId = UUID.randomUUID();
        ReflectionTestUtils.setField(slot, "id", slotId);
        when(scheduleRepository.findByPeriod(period)).thenReturn(List.of(slot));

        // 🔹 슬롯에 배치하려는 멤버 두 명 (UUID만 있으면 됨)
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();

        CompanyMember cm1 = mock(CompanyMember.class);
        CompanyMember cm2 = mock(CompanyMember.class);

        // 둘 다 WORKER + 고정 근무자 아님으로 세팅
        when(cm1.getRole()).thenReturn(MembershipRole.WORKER);
        when(cm1.isFixedShiftWorker()).thenReturn(false);
        when(cm2.getRole()).thenReturn(MembershipRole.WORKER);
        when(cm2.isFixedShiftWorker()).thenReturn(false);

        // 회사 소속이라고 리포지토리 스텁
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, m1))
                .thenReturn(Optional.of(cm1));
        when(companyMemberRepository.findByCompanyIdAndMemberId(companyId, m2))
                .thenReturn(Optional.of(cm2));

        // 요청: 정원 1명인 슬롯에 2명 넣기
        UpdateScheduleAssignmentsRequest.Item item = new UpdateScheduleAssignmentsRequest.Item();
        item.setScheduleId(slotId);
        item.setMemberIds(List.of(m1, m2)); // 2명 (정원 초과)

        UpdateScheduleAssignmentsRequest req = new UpdateScheduleAssignmentsRequest();
        req.setItems(List.of(item));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.updateScheduleAssignments(ownerId, companyId, periodId, req)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThat(slot.getAssignments()).isEmpty();
    }


    // =====================================================================
    // publishSchedulePeriod
    // =====================================================================
    @Test
    @DisplayName("근무표 확정 - DRAFT 상태에서 publish 호출")
    void publishSchedulePeriod_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = spy(SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        ));
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when
        scheduleService.publishSchedulePeriod(ownerId, companyId, periodId);

        // then
        verify(period, times(1)).publish(company.getOwner());
    }

    @Test
    @DisplayName("근무표 확정 - DRAFT가 아니면 예외")
    void publishSchedulePeriod_notDraft_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "사장");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID companyId = UUID.randomUUID();
        Company company = newCompany("카페 A", owner, "서울", "CODE1");
        ReflectionTestUtils.setField(company, "id", companyId);

        SchedulePeriod period = SchedulePeriod.create(
                company, "P1", PeriodType.WEEKLY,
                LocalDate.of(2025, 11, 17),
                LocalDate.of(2025, 11, 23),
                LocalDateTime.now().plusDays(1)
        );
        UUID periodId = UUID.randomUUID();
        ReflectionTestUtils.setField(period, "id", periodId);
        // 상태를 PUBLISHED 로 강제
        ReflectionTestUtils.setField(period, "status", PeriodStatus.PUBLISHED);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(schedulePeriodRepository.findById(periodId)).thenReturn(Optional.of(period));

        // when & then
        assertThatThrownBy(() ->
                scheduleService.publishSchedulePeriod(ownerId, companyId, periodId)
        ).isInstanceOf(IllegalStateException.class);
    }
}
