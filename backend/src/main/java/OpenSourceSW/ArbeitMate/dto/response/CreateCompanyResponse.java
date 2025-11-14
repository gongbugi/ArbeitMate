package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateCompanyResponse {
    UUID companyId;
    String name;
    String address;
    String inviteCode;
}
