package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateScheduleSlotRequest {
    @NotNull private UUID roleId;
    @NotNull private LocalDate workDate;
    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;
    @Min(1) private int requiredHeadCount;
}
