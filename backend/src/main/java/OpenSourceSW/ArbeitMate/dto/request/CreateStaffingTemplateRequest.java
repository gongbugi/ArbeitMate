package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateStaffingTemplateRequest {

    @NotBlank private String name;

    @NotEmpty @Valid
    private List<CreateStaffingTemplateItemRequest> items;
}
