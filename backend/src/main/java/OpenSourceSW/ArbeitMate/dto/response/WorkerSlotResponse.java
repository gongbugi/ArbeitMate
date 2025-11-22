package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class WorkerSlotResponse {
    UUID scheduleId;

    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;

    UUID roleId;
    String roleName;

    boolean recommended; // 추천 여부 (가능 시간에 속하는 지)
    boolean willing; // 이미 "가능"으로 제출한 슬롯인지
}
