package OpenSourceSW.ArbeitMate.security;

import java.io.Serializable;
import java.util.UUID;

public record AuthPrincipal(
        UUID memberId,
        String firebaseUid,
        String email) implements Serializable {

}
