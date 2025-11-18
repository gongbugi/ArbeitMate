package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CompanyWorkerResponse {
     UUID companyMemberId;
     String name;
     MembershipRole role;
     int hourlyWage;

     public static CompanyWorkerResponse from(CompanyMember cm) {
         return CompanyWorkerResponse.builder()
                 .companyMemberId(cm.getId())
                 .name(cm.getMember().getName())
                 .role(cm.getRole())
                 .hourlyWage(cm.getHourlyWage())
                 .build();
     }
}
