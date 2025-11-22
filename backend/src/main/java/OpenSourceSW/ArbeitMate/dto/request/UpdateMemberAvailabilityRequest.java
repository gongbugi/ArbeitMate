package OpenSourceSW.ArbeitMate.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UpdateMemberAvailabilityRequest {
    private List<MemberAvailabilityItemRequest> items;
}
