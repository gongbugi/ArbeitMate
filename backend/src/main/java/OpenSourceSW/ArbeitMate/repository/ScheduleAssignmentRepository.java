package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.ScheduleAssignment;
import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}