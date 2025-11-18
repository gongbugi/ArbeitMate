package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class UpdateCompanyRequest {
    @NotBlank private String name;
    @NotBlank private String address;
}
