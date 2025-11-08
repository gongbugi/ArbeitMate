package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.AvailabilitySubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AvailabilitySubmissionRepository extends JpaRepository<AvailabilitySubmission, UUID> {
    boolean existsByCompanyIdAndPeriodIdAndMemberId(UUID companyId, UUID periodId, UUID memberId);
}
