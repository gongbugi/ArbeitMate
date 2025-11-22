package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ScheduleAssignmentSlotResponse {
    UUID scheduleId;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    UUID roleId;
    String roleName;
    int requiredHeadcount;
    List<ScheduleAssignmentWorkerResponse> workers;

    public static ScheduleAssignmentSlotResponse from(Schedule schedule, Map<UUID, Boolean> fixedWorkerMap) {
        List<ScheduleAssignmentWorkerResponse> workers = schedule.getAssignments().stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED)
                .map(a -> ScheduleAssignmentWorkerResponse.from(a, fixedWorkerMap))
                .toList();

        return ScheduleAssignmentSlotResponse.builder()
                .scheduleId(schedule.getId())
                .workDate(schedule.getWorkDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .roleId(schedule.getRole().getId())
                .roleName(schedule.getRole().getName())
                .requiredHeadcount(schedule.getRequiredHeadcount())
                .workers(workers)
                .build();
    }
}
