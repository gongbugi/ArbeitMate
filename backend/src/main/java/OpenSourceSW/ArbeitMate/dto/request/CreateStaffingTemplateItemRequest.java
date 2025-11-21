package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateStaffingTemplateItemRequest {
    @Min(0) @Max(6)
    private int dow;

    @NotNull private LocalTime startTime;

    @NotNull private LocalTime endTime;

    @NotNull private UUID roleId;

    @Min(1)
    private int requiredHeadCount;
}
