package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.MemberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, UUID> {
    List<MemberAvailability> findByCompanyIdAndMemberId(UUID companyId, UUID memberId);

    void deleteByCompanyIdAndMemberId(UUID companyId, UUID memberId);
}
