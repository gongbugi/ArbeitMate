package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.CompanyNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyNoticeRepository extends JpaRepository<CompanyNotice, UUID> {
    Page<CompanyNotice> findByCompanyIdOrderByCreatedAtDesc(UUID companyId, Pageable pageable);
    List<CompanyNotice> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
