package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class FixedShiftItemRequest {
    @NotNull private UUID roleId;

    @Min(0) @Max(6)
    private int dow; // 0=월..6=일

    @NotNull private LocalTime startTime;

    @NotNull private LocalTime endTime;

    @NotNull private LocalDate effectiveFrom;

    private LocalDate effectiveTo; // null은 무기한
}
