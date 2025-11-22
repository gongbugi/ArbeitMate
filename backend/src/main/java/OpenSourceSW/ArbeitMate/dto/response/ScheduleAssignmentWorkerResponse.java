package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.ScheduleAssignment;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ScheduleAssignmentWorkerResponse {
    UUID memberId;
    String memberName;
    boolean fixedShiftWorker;

    public static ScheduleAssignmentWorkerResponse from(ScheduleAssignment a, Map<UUID, Boolean> fixedWorkerMap) {
        Member m = a.getMember();
        boolean fixed = fixedWorkerMap.getOrDefault(m.getId(), false);

        return ScheduleAssignmentWorkerResponse.builder()
                .memberId(m.getId())
                .memberName(m.getName())
                .fixedShiftWorker(fixed)
                .build();
    }
}
