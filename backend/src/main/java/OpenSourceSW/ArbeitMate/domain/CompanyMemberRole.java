package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 역할: 직원과 역할군 매핑 (다대다 관계)
 * 사용 예시: 자동편성시 해당 직원이 이 역할이 가능한지 검증
 */
@Entity
@Table(name = "company_member_roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_company_member_role", columnNames = {"company_id","member_id","role_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyMemberRole {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_member_role_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false)
    private CompanyRole role;

    //== 생성 메서드 ==//
    public static CompanyMemberRole link(Company company, Member member, CompanyRole role) {
        CompanyMemberRole cmr = new CompanyMemberRole();
        cmr.setCompany(company);
        cmr.setMember(member);
        cmr.setRole(role);
        return cmr;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
    }
    public void setMember(Member member) {
        this.member = member;
    }
    public void setRole(CompanyRole role) {
        this.role = role; // null 방지 안한 이유는 역할이 아직 안 정해진 경우 고려해서
        if (role != null) role.addMemberRole(this);
    }
}