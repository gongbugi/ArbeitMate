package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.FixedShift;
import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class FixedShiftItemResponse {
    UUID fixedShiftId;
    UUID roleId;
    String roleName;

    int dow; // 0=월..6=일

    LocalTime startTime;
    LocalTime endTime;

    LocalDate effectiveFrom;
    LocalDate effectiveTo;

    public static FixedShiftItemResponse from(FixedShift fs) {
        return FixedShiftItemResponse.builder()
                .fixedShiftId(fs.getId())
                .roleId(fs.getRole().getId())
                .roleName(fs.getRole().getName())
                .dow(fs.getDow())
                .startTime(fs.getStartTime())
                .endTime(fs.getEndTime())
                .effectiveFrom(fs.getEffectiveFrom())
                .effectiveTo(fs.getEffectiveTo())
                .build();
    }
}
