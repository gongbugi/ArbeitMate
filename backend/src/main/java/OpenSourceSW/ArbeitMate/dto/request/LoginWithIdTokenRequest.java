package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginWithIdTokenRequest {
    @NotBlank
    String idToken;
}
