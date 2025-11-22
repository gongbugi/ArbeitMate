package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AvailabilitySubmissionStatusResponse {
    UUID companyMemberId;
    UUID memberId;
    String memberName;

    boolean submitted;
    LocalDateTime submittedAt; // 미제출이면 null
}
