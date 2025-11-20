package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ScheduleSlotResponse {
    UUID scheduleId;
    UUID roleId;
    String roleName;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    int requiredHeadCount;

    public static ScheduleSlotResponse from(Schedule s) {
        return ScheduleSlotResponse.builder()
                .scheduleId(s.getId())
                .roleId(s.getRole().getId())
                .roleName(s.getRole().getName())
                .workDate(s.getWorkDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .requiredHeadCount(s.getRequiredHeadcount())
                .build();
    }
}
