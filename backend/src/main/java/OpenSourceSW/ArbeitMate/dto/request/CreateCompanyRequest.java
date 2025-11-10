package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCompanyRequest {
    @NotBlank
    private String companyName;
    @NotBlank
    private String companyAddress;
}
