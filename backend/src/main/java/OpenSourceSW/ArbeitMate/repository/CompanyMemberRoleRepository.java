package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyMemberRole;
import OpenSourceSW.ArbeitMate.domain.CompanyRole;
import OpenSourceSW.ArbeitMate.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyMemberRoleRepository extends JpaRepository<CompanyMemberRole, UUID> {
    List<CompanyMemberRole> findByCompanyIdAndMemberId(UUID companyId, UUID memberId);

    boolean existsByCompanyAndMemberAndRole(Company company, Member member, CompanyRole role);
}
