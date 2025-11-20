package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreateWeeklyPeriodRequest {
    private String name;
    @NotNull private LocalDate baseDate;
    @NotNull private LocalDateTime availabilityDueAt;
}
