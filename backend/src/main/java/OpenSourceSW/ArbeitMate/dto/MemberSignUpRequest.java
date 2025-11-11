package OpenSourceSW.ArbeitMate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignUpRequest {

    private String email;
    private String password;
    private String name;
    private String phone;
}