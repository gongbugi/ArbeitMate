package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.StaffingTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StaffingTemplateItemRepository extends JpaRepository<StaffingTemplateItem, UUID> {

}
