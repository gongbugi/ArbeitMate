package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateMonthlyPeriodRequest {
    private String name;

    @Min(2025)
    private int year;

    @Min(1) @Max(12)
    private int month;

    @NotNull private LocalDateTime availabilityDueAt;
}
