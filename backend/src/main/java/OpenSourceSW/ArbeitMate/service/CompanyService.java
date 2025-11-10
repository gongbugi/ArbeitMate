package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CreateCompanyResponse;
import OpenSourceSW.ArbeitMate.infra.InviteCodeGenerator;
import OpenSourceSW.ArbeitMate.repository.CompanyRepository;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    /**
     * 방(회사) 생성
     */
    public CreateCompanyResponse createCompany(/*Auth 추가예정,*/CreateCompanyRequest req) {
        // Auth 기반 member 찾기
        //

        String inviteCode = getUniqueInviteCode();
//        Company company = Company.create(
//                 req.getCompanyName(),
//                 member,
//                 req.getCompanyAddress(),
//                 inviteCodeGenerator.next());

//        UUID companyId = companyRepository.save(company).getId();

        return CreateCompanyResponse.builder()
                .companyId(null)
                .name(req.getCompanyName())
                .address(req.getCompanyAddress())
                .inviteCode(inviteCode)
                .build();
    }

    public String getUniqueInviteCode() {
        String tmp = inviteCodeGenerator.next();

        while(true) {
            Optional<Company> c = companyRepository.findByInviteCode(tmp);
            if(!c.isPresent()) return tmp;
            else tmp = inviteCodeGenerator.next();
        }
    }

    /**
     * 방(회사) 참가
     */

}
