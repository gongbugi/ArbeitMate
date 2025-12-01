package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SalaryResponse {
    private int year;
    private int month;

    private int hourlyWage;
    private int totalMinutes;

    private int baseSalary;       // 기본 급여 (시간 * 시급)
    private int holidayAllowance; // 주휴수당 (조건 만족 시)
    private int totalSalary;      // 최종 예상 급여 (기본급 + 주휴수당)

    // 근무 식별 ID
    private UUID scheduleAssignmentId;

    private List<SalaryDetail> details;

    @Getter
    @Builder
    public static class SalaryDetail {
        private LocalDate date;
        private String startTime;
        private String endTime;
        private int workMinutes;
        private int dailySalary;

        private UUID scheduleAssignmentId;
    }
}