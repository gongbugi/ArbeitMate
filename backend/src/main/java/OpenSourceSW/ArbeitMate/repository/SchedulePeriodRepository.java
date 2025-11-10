package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SchedulePeriodRepository extends JpaRepository<SchedulePeriod, UUID> {
    List<SchedulePeriod> findByCompanyIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID companyId, PeriodStatus status, LocalDate start, LocalDate end);
}
