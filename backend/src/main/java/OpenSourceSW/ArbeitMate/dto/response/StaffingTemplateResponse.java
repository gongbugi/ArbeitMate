package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.StaffingTemplate;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StaffingTemplateResponse {
    UUID templateId;
    String name;
    List<StaffingTemplateItemResponse> items;

    public static StaffingTemplateResponse from(StaffingTemplate template) {
        return StaffingTemplateResponse.builder()
                .templateId(template.getId())
                .name(template.getName())
                .items(template.getItems().stream()
                                .map(StaffingTemplateItemResponse::from)
                                .toList())
                .build();
    }
}
