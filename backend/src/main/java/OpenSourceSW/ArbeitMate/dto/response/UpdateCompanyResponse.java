package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Company;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateCompanyResponse {
    UUID companyId;
    String name;
    String address;
    String inviteCode;

    public static UpdateCompanyResponse from(Company company) {
        return UpdateCompanyResponse.builder()
                .companyId(company.getId())
                .name(company.getName())
                .address(company.getAddress())
                .inviteCode(company.getInviteCode())
                .build();
    }
}
