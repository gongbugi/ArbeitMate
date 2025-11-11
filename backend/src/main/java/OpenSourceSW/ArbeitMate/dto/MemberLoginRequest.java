// src/main/java/OpenSourceSW/ArbeitMate/dto/MemberLoginRequest.java
package OpenSourceSW.ArbeitMate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberLoginRequest {

    private String email;
    private String password;
}