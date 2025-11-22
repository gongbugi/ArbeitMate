package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MemberAvailabilityResponse {
    UUID memberId;
    UUID companyId;
    String memberName;
    List<MemberAvailabilityItemResponse> items;
}
