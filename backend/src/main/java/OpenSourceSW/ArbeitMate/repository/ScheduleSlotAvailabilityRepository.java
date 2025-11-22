package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.SchedulePeriod;
import OpenSourceSW.ArbeitMate.domain.ScheduleSlotAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScheduleSlotAvailabilityRepository extends JpaRepository<ScheduleSlotAvailability, UUID> {
    @Query("select a from ScheduleSlotAvailability a " +
            "where a.member.id = :memberId and a.schedule.period = :period")
    List<ScheduleSlotAvailability> findByMemberAndPeriod(@Param("memberId") UUID memberId, @Param("period") SchedulePeriod period);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ScheduleSlotAvailability a " +
            "where a.member.id = :memberId and a.schedule.period = :period")
    void deleteByMemberAndPeriod(@Param("memberId") UUID memberId, @Param("period") SchedulePeriod period);

    @Query("""
           select a from ScheduleSlotAvailability a
           where a.schedule.period = :period
           """)
    List<ScheduleSlotAvailability> findByPeriod(@Param("period") SchedulePeriod period);
}
