package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.ScheduleAssignment;
import OpenSourceSW.ArbeitMate.dto.response.SalaryResponse;
import OpenSourceSW.ArbeitMate.repository.CompanyMemberRepository;
import OpenSourceSW.ArbeitMate.repository.ScheduleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryService {

    private final ScheduleAssignmentRepository scheduleAssignmentRepository;
    private final CompanyMemberRepository companyMemberRepository;

    @Value("${hourlyWage}")
    private int MINIMUM_WAGE_2025;

    public SalaryResponse calculateMonthlySalary(UUID memberId, UUID companyId, int year, int month) {
        int hourlyWage = MINIMUM_WAGE_2025;

        Optional<CompanyMember> companyMemberOpt = companyMemberRepository.findByMemberIdAndCompanyId(memberId, companyId);
        if (companyMemberOpt.isPresent()) {
             hourlyWage = Math.max(MINIMUM_WAGE_2025, companyMemberOpt.get().getHourlyWage());
        }

        // 2. 조회 기간 설정
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 3. 근무 기록 조회
        List<ScheduleAssignment> assignments = scheduleAssignmentRepository.findMonthlyAssignments(memberId, startDate, endDate);

        // 4. 계산 로직 (주휴수당 포함)
        int totalMinutes = 0;
        int baseSalary = 0;

        // 주별 근무 시간 합산을 위한 맵 (Key: 주차(WeekOfYear), Value: 분(Minutes))
        Map<Integer, Long> weeklyMinutesMap = new HashMap<>();
        List<SalaryResponse.SalaryDetail> details = new ArrayList<>();

        // ISO 기준 주차 사용 (월요일 시작)
        WeekFields weekFields = WeekFields.ISO;

        for (ScheduleAssignment sa : assignments) {
            var s = sa.getSchedule();

            // 근무 시간 계산
            long minutes = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();

            // 일급 계산
            int dailySalary = (int) ((minutes / 60.0) * hourlyWage);

            // 총합 누적
            totalMinutes += (int) minutes;
            baseSalary += dailySalary;

            // 주차별 시간 누적 (주휴수당 계산용)
            int weekOfYear = s.getWorkDate().get(weekFields.weekOfYear());
            weeklyMinutesMap.merge(weekOfYear, minutes, Long::sum);

            details.add(SalaryResponse.SalaryDetail.builder()
                    .date(s.getWorkDate())
                    .startTime(s.getStartTime().toString())
                    .endTime(s.getEndTime().toString())
                    .workMinutes((int) minutes)
                    .dailySalary(dailySalary)
                    .build());
        }

        // 5. 주휴수당 계산
        int holidayAllowance = 0;

        for (long weeklyMinutes : weeklyMinutesMap.values()) {
            if (weeklyMinutes >= 900) { // 15시간 이상
                double weeklyHours = weeklyMinutes / 60.0;

                // 주 40시간까지만 비례 인정 (최대 8시간)
                double calcHours = Math.min(weeklyHours, 40.0);

                // 주휴수당 공식 적용
                int allowance = (int) ((calcHours / 40.0) * 8 * hourlyWage);
                holidayAllowance += allowance;
            }
        }

        return SalaryResponse.builder()
                .year(year)
                .month(month)
                .hourlyWage(hourlyWage)
                .totalMinutes(totalMinutes)
                .baseSalary(baseSalary)
                .holidayAllowance(holidayAllowance) // 주휴수당 포함
                .totalSalary(baseSalary + holidayAllowance) // 최종 합계
                .details(details)
                .build();
    }
}