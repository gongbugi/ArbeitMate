package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    List<CompanyMember> findByMemberId(UUID memberId);
    List<CompanyMember> findByCompanyId(UUID companyId);
    List<CompanyMember> findByCompanyIdAndRole(UUID companyId, MembershipRole role);
    Optional<CompanyMember> findByCompanyIdAndMemberId(UUID companyId, UUID memberId);
}
