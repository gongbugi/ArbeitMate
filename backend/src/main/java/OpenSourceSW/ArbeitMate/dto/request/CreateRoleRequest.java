package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class CreateRoleRequest {
    @NotBlank private String name; // 예) 홀, 주방, 서빙 등
}
