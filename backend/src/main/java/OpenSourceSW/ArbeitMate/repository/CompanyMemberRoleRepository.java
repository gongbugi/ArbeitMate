package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.CompanyMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyMemberRoleRepository extends JpaRepository<CompanyMemberRole, UUID> {
    List<CompanyMemberRole> findByCompanyIdAndMemberId(UUID companyId, UUID memberId);
}
