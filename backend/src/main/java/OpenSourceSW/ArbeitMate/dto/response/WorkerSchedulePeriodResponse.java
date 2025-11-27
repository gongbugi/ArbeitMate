package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkerSchedulePeriodResponse {

    private UUID periodId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime availabilityDueAt;
    private PeriodStatus status;
    private boolean submitted; //제출 여부
    private LocalDateTime submittedAt; //제출 날짜 (제출했을 경우만)

    public static WorkerSchedulePeriodResponse from(
            SchedulePeriod p,
            boolean submitted,
            LocalDateTime submittedAt
    ) {
        return WorkerSchedulePeriodResponse.builder()
                .periodId(p.getId())
                .name(p.getName())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .availabilityDueAt(p.getAvailabilityDueAt())
                .status(p.getStatus())
                .submitted(submitted)
                .submittedAt(submittedAt)
                .build();
    }
}