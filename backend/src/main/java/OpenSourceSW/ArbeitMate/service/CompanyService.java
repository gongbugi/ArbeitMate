package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.CompanyRole;
import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.dto.request.CreateRoleRequest;
import OpenSourceSW.ArbeitMate.dto.request.CreateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.UpdateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.*;
import OpenSourceSW.ArbeitMate.infra.InviteCodeGenerator;
import OpenSourceSW.ArbeitMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final CompanyRoleRepository companyRoleRepository;
    private final CompanyMemberRoleRepository companyMemberRoleRepository;
    private final AvailabilitySubmissionRepository availabilitySubmissionRepository;
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

    /**
     *  회사 기본 정보 수정
     */
    @Transactional
    public UpdateCompanyResponse updateCompany(UUID memberId, UUID companyId, UpdateCompanyRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(memberId, company);

        // 변경 감지를 통해 저장
        company.updateInfo(req.getName(), req.getAddress());

        return UpdateCompanyResponse.from(company);
    }

    /**
     * 초대코드 재생성
     */
    @Transactional
    public UpdateCompanyResponse regenerateInviteCode(UUID memberId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(memberId, company);

        String newCode = getUniqueInviteCode();
        company.applyInviteCode(newCode);

        return UpdateCompanyResponse.from(company);
    }

    /**
     * 회사 삭제
     */
    @Transactional
    public void deleteCompany(UUID memberId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(memberId, company);

        companyRepository.delete(company); // 하위 엔티티는 cascade + orphanRemoval로 함께 자동으로 삭제
    }

    /**
     *  알바생 목록 조회
     */
    public List<CompanyWorkerResponse> listWorkers(UUID ownerId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        return company.getCompanyMembers().stream()
                .filter(cm -> cm.getRole() != MembershipRole.OWNER) // 사장 본인은 제외
                .map(CompanyWorkerResponse::from)
                .toList();
    }

    /**
     * 매장에서 알바생 제외
     */
    @Transactional
    public void removeWorker(UUID ownerId, UUID companyId, UUID companyMemberId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        // 해당 CompanyMember 찾기
        CompanyMember target = company.getCompanyMembers().stream()
                .filter(cm -> cm.getId().equals(companyMemberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("CompanyMember not found"));

        if (target.getRole() == MembershipRole.OWNER) {
            throw new IllegalStateException("사장 계정은 매장에서 제외할 수 없습니다.");
        }

        Member member = target.getMember();
        availabilitySubmissionRepository.deleteByCompanyAndMember(company, member);
        company.removeCompanyMember(target); // 변경 감지를 통해 저장
    }

    /**
     * 역할군 추가
     */
    @Transactional
    public CompanyRoleResponse createRole(UUID ownerId, UUID companyId, CreateRoleRequest req) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateOwner(ownerId, company);

        if(companyRoleRepository.existsByCompanyIdAndName(companyId, req.getName())) {
            throw new IllegalStateException("이미 이 매장에 존재하는 역할 이름입니다.");
        }

        CompanyRole role = CompanyRole.create(company, req.getName());
        companyRoleRepository.save(role);

        return CompanyRoleResponse.from(role);
    }

    /**
     * 역할군 목록 조회
     */
    public List<CompanyRoleResponse> listRoles(UUID memberId, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        validateMembersBelongToCompany(memberId, company);

        return company.getRoles().stream()
                .map(CompanyRoleResponse::from)
                .toList();
    }

    // 중복 확인
    protected boolean isAlreadyJoined(Member member, Company company) {
        return company.getCompanyMembers().stream()
                .anyMatch(cm -> cm.getMember().getId().equals(member.getId()));
    }
    // Owner 확인
    private void validateOwner(UUID memberId, Company company) {
        if (!company.getOwner().getId().equals(memberId)) {
            throw new IllegalStateException("해당 매장의 사장만 이 작업을 수행할 수 있습니다.");
        }
    }
    // 회사 소속 확인
    private void validateMembersBelongToCompany(UUID memberId, Company company) {
        if(company.getCompanyMembers().stream()
                .noneMatch(cm -> cm.getMember().getId().equals(memberId))) {
            throw new IllegalStateException("해당 매장에 속한 멤버가 아닙니다.");
        }
    }
}
