package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkerAvailabilitySlotsResponse {
    List<WorkerSlotResponse> recommendedSlots;
    List<WorkerSlotResponse> otherSlots;
}
