package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.ScheduleSlotAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleSlotAvailabilityRepository extends JpaRepository<ScheduleSlotAvailability, UUID> {

}
