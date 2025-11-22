package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FixedShiftResponse {
    UUID companyMemberId;
    UUID memberId;
    String memberName;

    boolean fixedShiftWorker;

    List<FixedShiftItemResponse> shifts;
}
