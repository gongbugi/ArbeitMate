package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.SwapRequest;
import OpenSourceSW.ArbeitMate.domain.enums.SwapStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, UUID> {
    List<SwapRequest> findByCompanyIdAndStatus(UUID companyId, SwapStatus status);
}