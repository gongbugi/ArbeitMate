package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank private String name; // 예) 홀, 주방, 서빙 등
}
