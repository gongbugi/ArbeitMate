package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class MemberAvailabilityItemRequest {
    @Min(0) @Max(6)
    private int dow;

    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;

    @NotNull private LocalDate effectiveFrom;
    LocalDate effectiveTo; //null은 무기한
}
