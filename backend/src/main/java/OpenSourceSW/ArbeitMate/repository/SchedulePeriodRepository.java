package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SchedulePeriodRepository extends JpaRepository<SchedulePeriod, UUID> {
    List<SchedulePeriod> findByCompanyIdAndStatusAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            UUID companyId, PeriodStatus status, LocalDate start, LocalDate end);

    boolean existsByCompanyIdAndName(UUID companyId, String name);

    /// 시작기간과 종료기간이 겹치는게 존재하는지 검사
    @Query("""
        select case when count(p) > 0 then true else false end
        from SchedulePeriod p
        where p.company.id = :companyId
          and p.startDate <= :endDate
          and p.endDate >= :startDate
    """)
    boolean existsOverlapping(UUID companyId, LocalDate startDate, LocalDate endDate);
}
