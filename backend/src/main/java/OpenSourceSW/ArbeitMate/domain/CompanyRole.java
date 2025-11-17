package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 역할: 매장 커스텀 역할군 정의 (예: 홀, 주방, 서빙 등)
 * 사용 예시: 스케쥴 슬롯/템플릿에서 요구 역할 연결, 직원 역할 부여
 */
@Entity
@Table(name = "company_roles",
        uniqueConstraints = @UniqueConstraint(name = "uq_company_role_name", columnNames = {"company_id","name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyRole {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, unique = true)
    private String name; // 예: 홀, 주방

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMemberRole> memberRoles = new ArrayList<>();

    //== 생성 메서드 ==//
    public static CompanyRole create(Company company, String name) {
        CompanyRole r = new CompanyRole();
        r.setCompany(company);
        r.name = name;
        return r;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) company.addRole(this);
    }

    public void addMemberRole(CompanyMemberRole cmr) {
        if (!this.memberRoles.contains(cmr)) {
            this.memberRoles.add(cmr);
            cmr.setRole(this);
        }
    }
}