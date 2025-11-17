package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.AvailabilitySubmission;
import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AvailabilitySubmissionRepository extends JpaRepository<AvailabilitySubmission, UUID> {
    boolean existsByCompanyIdAndPeriodIdAndMemberId(UUID companyId, UUID periodId, UUID memberId);
    void deleteByCompanyAndMember(Company company, Member member);
}
