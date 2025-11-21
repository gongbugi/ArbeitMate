package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SchedulePeriodWithSlotsResponse {

    UUID periodId;
    String name;
    PeriodType periodType;
    PeriodStatus status;
    LocalDate startDate;
    LocalDate endDate;

    private List<ScheduleSlotResponse> slots;

    public static SchedulePeriodWithSlotsResponse of(SchedulePeriod period, List<Schedule> schedules) {
        return SchedulePeriodWithSlotsResponse.builder()
                .periodId(period.getId())
                .name(period.getName())
                .periodType(period.getPeriodType())
                .status(period.getStatus())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .slots(schedules.stream()
                        .map(ScheduleSlotResponse::from)
                        .toList())
                .build();
    }
}
