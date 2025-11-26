package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MyCompanyResponse {
    UUID companyId;
    String companyName;
    MembershipRole role;

    public static MyCompanyResponse from(CompanyMember cm) {
        return MyCompanyResponse.builder()
                .companyId(cm.getCompany().getId())
                .companyName(cm.getCompany().getName())
                .role(cm.getRole())
                .build();
    }
}
