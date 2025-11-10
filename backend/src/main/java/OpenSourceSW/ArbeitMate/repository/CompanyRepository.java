package OpenSourceSW.ArbeitMate.repository;

import OpenSourceSW.ArbeitMate.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByOwnerId(UUID ownerId);
    Optional<Company> findByInviteCode(String inviteCode);

    @Query("""
      select c
      from Company c
      where c.owner.id = :memberId
        or exists (
            select 1
            from CompanyMember cm
            where cm.company = c and cm.member.id = :memberId
        )
      order by c.createdAt desc
    """)
    List<Company> findMyCompanies(UUID memberId); // 내가 속한 회사 확인 (사장, 알바 둘 다 포함)
}