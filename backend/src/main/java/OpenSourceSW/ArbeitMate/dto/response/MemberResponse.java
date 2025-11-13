package OpenSourceSW.ArbeitMate.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberResponse {
    String id;
    String email;
    String name;
    String firebaseUid;
}
