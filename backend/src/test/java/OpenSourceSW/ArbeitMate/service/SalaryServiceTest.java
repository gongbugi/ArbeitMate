package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.ScheduleAssignment;
import OpenSourceSW.ArbeitMate.dto.response.SalaryResponse;
import OpenSourceSW.ArbeitMate.repository.CompanyMemberRepository;
import OpenSourceSW.ArbeitMate.repository.ScheduleAssignmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

class SalaryServiceTest {

    private SalaryService salaryService;

    @Mock
    private ScheduleAssignmentRepository scheduleAssignmentRepository;

    @Mock
    private CompanyMemberRepository companyMemberRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        salaryService = new SalaryService(scheduleAssignmentRepository, companyMemberRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("급여 계산 - 근무 기록이 없으면 0원")
    void calculateMonthlySalary_noWork() {
        UUID memberId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        lenient().when(scheduleAssignmentRepository.findMonthlyAssignments(any(), any(), any()))
                .thenReturn(List.of());

        SalaryResponse response = salaryService.calculateMonthlySalary(memberId, companyId, 2025, 11);

        assertThat(response.getBaseSalary()).isEqualTo(0);
        assertThat(response.getTotalSalary()).isEqualTo(0);
    }

    @Test
    @DisplayName("급여 계산 - 주 15시간 미만 (5시간 근무)")
    void calculateMonthlySalary_under15Hours() {
        UUID memberId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        // 1. Mock 객체 생성
        ScheduleAssignment work1 = createMockAssignment(2025, 11, 3, 5);
        List<ScheduleAssignment> mockList = List.of(work1);

        // 2. 리포지토리 설정
        setupRepositoryToReturn(mockList);
        setupCompanyMember();

        // 3. 실행
        SalaryResponse response = salaryService.calculateMonthlySalary(memberId, companyId, 2025, 11);

        // 4. 검증
        assertThat(response.getBaseSalary()).isEqualTo(50150);
        assertThat(response.getHolidayAllowance()).isEqualTo(0);
    }

    @Test
    @DisplayName("급여 계산 - 주 15시간 이상 (15시간 근무)")
    void calculateMonthlySalary_over15Hours() {
        UUID memberId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        // 1. Mock 객체 생성
        ScheduleAssignment w1 = createMockAssignment(2025, 11, 3, 5);
        ScheduleAssignment w2 = createMockAssignment(2025, 11, 4, 5);
        ScheduleAssignment w3 = createMockAssignment(2025, 11, 5, 5);
        List<ScheduleAssignment> mockList = List.of(w1, w2, w3);

        // 2. 리포지토리 설정
        setupRepositoryToReturn(mockList);
        setupCompanyMember();

        // 3. 실행
        SalaryResponse response = salaryService.calculateMonthlySalary(memberId, companyId, 2025, 11);

        // 4. 검증
        int expectedBase = 15 * 10030;
        int expectedHoliday = (int) ((15.0 / 40.0) * 8 * 10030);

        assertThat(response.getBaseSalary()).isEqualTo(expectedBase);
        assertThat(response.getHolidayAllowance()).isEqualTo(expectedHoliday);
    }

    private void setupRepositoryToReturn(List<ScheduleAssignment> list) {
        lenient().when(scheduleAssignmentRepository.findMonthlyAssignments(any(), any(), any()))
                .thenAnswer(invocation -> list);
    }

    private void setupCompanyMember() {
        CompanyMember mockMember = mock(CompanyMember.class);
        // 시급 설정 (중요)
        lenient().when(mockMember.getHourlyWage()).thenReturn(10030);

        lenient().when(companyMemberRepository.findByMemberIdAndCompanyId(any(), any()))
                .thenReturn(Optional.of(mockMember));
    }

    private ScheduleAssignment createMockAssignment(int year, int month, int day, int hours) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = start.plusHours(hours);

        Schedule s = mock(Schedule.class);

        lenient().when(s.getWorkDate()).thenReturn(date);
        lenient().when(s.getStartTime()).thenReturn(start);
        lenient().when(s.getEndTime()).thenReturn(end);

        ScheduleAssignment sa = mock(ScheduleAssignment.class);
        lenient().when(sa.getSchedule()).thenReturn(s);

        return sa;
    }
}