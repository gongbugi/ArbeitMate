package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.MemberAvailability;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class MemberAvailabilityItemResponse {
    UUID memberAvailabilityId;
    int dow;
    LocalTime startTime;
    LocalTime endTime;
    LocalDate effectiveFrom;
    LocalDate effectiveTo;

    public static MemberAvailabilityItemResponse from(MemberAvailability a) {
        return MemberAvailabilityItemResponse.builder()
                .memberAvailabilityId(a.getId())
                .dow(a.getDow())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .effectiveFrom(a.getEffectiveFrom())
                .effectiveTo(a.getEffectiveTo())
                .build();
    }
}
