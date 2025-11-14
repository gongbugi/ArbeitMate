package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CreateCompanyResponse;
import OpenSourceSW.ArbeitMate.dto.response.ParticipateCompanyResponse;
import OpenSourceSW.ArbeitMate.infra.InviteCodeGenerator;
import OpenSourceSW.ArbeitMate.repository.CompanyRepository;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${hourlyWage}")
    private int currentHourlyWage;

    /**
     * 방(회사) 생성
     */
    @Transactional
    public CreateCompanyResponse createCompany(UUID memberId, CreateCompanyRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        // 회사 생성
        String inviteCode = getUniqueInviteCode();
        Company company = Company.create(
                 req.getCompanyName(),
                 member,
                 req.getCompanyAddress(),
                 inviteCode);
        
        // 회사의 주인으로 추가
        CompanyMember cm = CompanyMember.create(company, member, MembershipRole.OWNER, currentHourlyWage);

        UUID companyId = companyRepository.save(company).getId();

        return CreateCompanyResponse.builder()
                .companyId(companyId)
                .name(req.getCompanyName())
                .address(req.getCompanyAddress())
                .inviteCode(inviteCode)
                .build();
    }

    protected String getUniqueInviteCode() {
        String code;
        do {
            code = inviteCodeGenerator.next();
        } while (companyRepository.findByInviteCode(code).isPresent());
        return code;
    }

    /**
     * 방(회사) 참가
     */
    @Transactional
    public ParticipateCompanyResponse participateCompany(UUID memberId, ParticipateCompanyRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Company company = companyRepository.findByInviteCode(req.getInviteCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Invite code"));
        
        // 중복 가입 방지
        if(isAlreadyJoined(member, company)) {
            throw new IllegalStateException("이미 이 매장에 가입된 회원입니다.");
        }

        // 생성시 기본 역할: Worker, 시급: 최저시급 (추후 사장이 조정 가능)
        CompanyMember cm = CompanyMember.create(company, member, MembershipRole.WORKER, currentHourlyWage); // 변경 감지를 통해 저장

        return ParticipateCompanyResponse.builder()
                .companyId(company.getId())
                .build();
    }

    protected boolean isAlreadyJoined(Member member, Company company) {
        return company.getCompanyMembers().stream()
                .anyMatch(cm -> cm.getMember().getId().equals(member.getId()));
    }
}
