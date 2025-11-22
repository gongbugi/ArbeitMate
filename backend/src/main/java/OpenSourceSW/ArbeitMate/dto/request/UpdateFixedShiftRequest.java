package OpenSourceSW.ArbeitMate.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateFixedShiftRequest {
    private boolean fixedShiftWorker; // true: 고정 근무자 / false: 고정 근무자 해제 + 기존 고정근무 패턴 삭제

    private List<FixedShiftItemRequest> shifts; // 고정 근무 시간 (false면 무시)
}