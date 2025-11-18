package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.CompanyRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CompanyRoleResponse {
    UUID roleId;
    String name;

    public static CompanyRoleResponse from(CompanyRole role) {
        return CompanyRoleResponse.builder()
                .roleId(role.getId())
                .name(role.getName())
                .build();
    }
}
