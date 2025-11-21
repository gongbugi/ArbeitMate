package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SchedulePeriodResponse {
    UUID periodId;
    String name;
    PeriodType periodType;
    LocalDate startDate;
    LocalDate endDate;
    PeriodStatus status;
    LocalDateTime availabilityDueAt;

    public static SchedulePeriodResponse from(SchedulePeriod p) {
        return SchedulePeriodResponse.builder()
                .periodId(p.getId())
                .name(p.getName())
                .periodType(p.getPeriodType())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .availabilityDueAt(p.getAvailabilityDueAt())
                .build();
    }
}
