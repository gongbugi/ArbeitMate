package OpenSourceSW.ArbeitMate.dto.request;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class UpdateCompanyRequest {
    private String name;
    private String address;
}
