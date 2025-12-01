package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus; // 이게 있는지 확인 필요
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class ScheduleResponse {

    UUID scheduleId;
    UUID roleId;
    String roleName;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    int requiredHeadCount;

    @Data
    @Builder
    public static class AssignedWorker {
        private String workerName;          // 이름
        private UUID memberId;              // 멤버 ID (사용자 식별용)
        private UUID scheduleAssignmentId;  // ★ 근무 교환 신청할 때 필수!
    }

    int currentHeadCount;
    
    // id포함된 리스트
    List<AssignedWorker> assignedWorkers;

    public static ScheduleResponse from(Schedule s) {

        List<AssignedWorker> workers = s.getAssignments().stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED)
                .map(a -> AssignedWorker.builder()
                        .workerName(a.getMember().getName())
                        .memberId(a.getMember().getId())
                        .scheduleAssignmentId(a.getId())
                        .build())
                .collect(Collectors.toList());

        return ScheduleResponse.builder()
                .scheduleId(s.getId())
                .roleId(s.getRole().getId())
                .roleName(s.getRole().getName())
                .workDate(s.getWorkDate())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .requiredHeadCount(s.getRequiredHeadcount())

                .assignedWorkers(workers)
                .currentHeadCount(workers.size())
                .build();
    }
}