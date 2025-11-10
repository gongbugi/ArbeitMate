package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRoleRepository extends JpaRepository<CompanyRole, UUID> {
    List<CompanyRole> findByCompanyId(UUID companyId);
    boolean existsByCompanyIdAndName(UUID companyId, String name);
}