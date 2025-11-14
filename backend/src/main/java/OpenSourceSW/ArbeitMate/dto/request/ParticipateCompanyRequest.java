package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParticipateCompanyRequest {
    @NotBlank private String inviteCode;
}
