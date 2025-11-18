package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
public class SignupEmailRequest {
    @Email @NotBlank String email;
    @NotBlank String password;
    @NotBlank String name;
}
