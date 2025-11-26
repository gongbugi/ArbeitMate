package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.ScheduleAssignment;
import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public interface ScheduleAssignmentRepository extends JpaRepository<ScheduleAssignment, UUID> {
    List<ScheduleAssignment> findByMemberIdAndStatus(UUID memberId, AssignmentStatus status);

    @Query("""
           select sa from ScheduleAssignment sa
           join sa.schedule s
           join s.period p
           where sa.member.id = :memberId
             and p.status = OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus.PUBLISHED
           """)
    List<ScheduleAssignment> findMyAssignmentsInPublished(UUID memberId);

    @Query("SELECT sa FROM ScheduleAssignment sa " +
            "JOIN FETCH sa.schedule s " +
            "WHERE sa.member.id = :memberId " +
            "AND sa.status = OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus.ASSIGNED " +
            "AND s.workDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.workDate ASC, s.startTime ASC")
    List<ScheduleAssignment> findMonthlyAssignments(
            @Param("memberId") UUID memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}