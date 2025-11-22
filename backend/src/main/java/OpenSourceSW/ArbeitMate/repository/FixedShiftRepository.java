package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.FixedShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FixedShiftRepository extends JpaRepository<FixedShift, UUID> {
    List<FixedShift> findByCompanyIdAndMemberId(UUID companyId, UUID memberId);
    List<FixedShift> findByCompanyId(UUID companyId);

    @Modifying
    @Query("delete from FixedShift fs where fs.company.id = :companyId and fs.member.id = :memberId")
    void deleteByCompanyIdAndMemberId(@Param("companyId") UUID companyId, @Param("memberId") UUID memberId);

    boolean existsByCompanyIdAndMemberId(UUID companyId, UUID memberId);

    @Query("""
        select f from FixedShift f
        where f.company.id = :companyId
          and f.effectiveFrom <= :end
          and (f.effectiveTo is null or f.effectiveTo >= :start)
        """)
    List<FixedShift> findActiveInPeriod(@Param("companyId") UUID companyId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
