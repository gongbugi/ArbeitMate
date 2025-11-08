package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.StaffingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StaffingTemplateRepository extends JpaRepository<StaffingTemplate, UUID> {
    List<StaffingTemplate> findByCompanyId(UUID companyId);
    boolean existsByCompanyIdAndName(UUID companyId, String name);
}
