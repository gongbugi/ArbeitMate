package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findByCompanyIdAndWorkDateBetween(UUID companyId, LocalDate from, LocalDate to);
    List<Schedule> findByPeriod(SchedulePeriod period);
    void deleteByPeriod(SchedulePeriod period);
}
