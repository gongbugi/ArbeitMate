package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CreateCompanyResponse;
import OpenSourceSW.ArbeitMate.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    public CreateCompanyResponse createCompany(/*@Authentication ~~ (로그인 기능 개발 후 추가 예정) ,*/@Valid @RequestBody CreateCompanyRequest req) {
        return companyService.createCompany(/*,*/ req);
    }
}
