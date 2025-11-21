package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.Schedule;
import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findByCompanyIdAndWorkDateBetween(UUID companyId, LocalDate from, LocalDate to);
    List<Schedule> findByPeriod(SchedulePeriod period);
    @Modifying
    @Query("delete from Schedule s where s.period.id = :periodId")
    void deleteByPeriodId(@Param("periodId") UUID periodId);
}
