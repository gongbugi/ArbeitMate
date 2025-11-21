package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.StaffingTemplateItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class StaffingTemplateItemResponse {
    int dow;
    LocalTime startTime;
    LocalTime endTime;
    UUID roleId;
    String roleName;
    int requiredHeadCount;

    public static StaffingTemplateItemResponse from(StaffingTemplateItem item) {
        return StaffingTemplateItemResponse.builder()
                .dow(item.getDow())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .roleId(item.getRole().getId())
                .roleName(item.getRole().getName())
                .requiredHeadCount(item.getHeadcount())
                .build();
    }
}