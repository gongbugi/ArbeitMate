package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitAvailabilityRequest {
    private List<UUID> slotIds; // 가능한 슬롯이 없을 경우에 빈 슬롯도 가능
}
