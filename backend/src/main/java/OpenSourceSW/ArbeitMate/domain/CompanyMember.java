package OpenSourceSW.ArbeitMate.domain;

import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 역할: 멤버–매장 소속확인, 기본 권한(사장, 알바 등)확인, 개별 시급 설정, 알바 전 알림시간 설정
 * 사용 예시: 시급 게산, 권한 체크 등
 */
@Entity
@Table(name = "company_members",
        uniqueConstraints = @UniqueConstraint(name = "uq_company_member", columnNames = {"company_id","member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyMember {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_member_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MembershipRole role; // OWNER/MANAGER/WORKER

    @Column(nullable = false, precision = 10, scale = 2)
    private int hourlyWage;

    @Column(nullable = false)
    private Integer alertLeadMinutes = 120; // 기본 값: 2시간 전 알림

    //== 생성 메서드 ==//
    public static CompanyMember create(Company company, Member member, MembershipRole role, int hourlyWage) {
        CompanyMember cm = new CompanyMember();
        cm.setCompany(company);
        cm.setMember(member);
        cm.role = role;
        cm.hourlyWage = hourlyWage;
        cm.alertLeadMinutes = 120;
        return cm;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) company.addCompanyMember(this);
    }
    public void setMember(Member member) {
        this.member = member;
        if (member != null) member.addMembership(this);
    }

    //== 비즈니스 로직==//
    /**
     * 알림 시간 재조정
     */
    public void adjustAlertLeadMinutes(int minutes) {
        this.alertLeadMinutes = minutes;
    }
}