package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

/**
 * 역할: 매장(방), 사장 1명이 소유함
 * 사용 예시: 매장 생성/관리, 초대 코드로 직원 가입
 */
@Entity @Table(name = "companies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Company {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "company_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false) private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id", nullable = false)
    private Member owner;

    // 주소(단일 문자열로 작성)
    @Column(nullable = false) private String address;

    @Column(nullable = false, unique = true) private String inviteCode;

    @Column(nullable = false) private LocalDateTime inviteCodeGeneratedAt;

    @Column(nullable = false) private LocalDateTime createdAt;

    // 하위 집합들
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMember> companyMembers = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SchedulePeriod> periods = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffingTemplate> templates = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AvailabilitySubmission> availabilitySubmissions = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAvailability> memberAvailabilities = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyNotice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SwapRequest> swapRequests = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (inviteCodeGeneratedAt == null) inviteCodeGeneratedAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static Company create(String name, Member owner, String address, String inviteCode) {
        Company c = new Company();
        c.name = name;
        c.owner = owner;
        c.address = address;

        // 초대코드는 Domain 에서 Unique 로 중복 방지 제약조건은 걸었으나, 초대코드 생성 및 중복 체크는 Service 에서 코드로 작성
        c.applyInviteCode(inviteCode);
        return c;
    }

    //== 연관관계 편의 메서드 ==//
    public void addCompanyMember(CompanyMember cm) {
        if (!this.companyMembers.contains(cm)) {
            this.companyMembers.add(cm);
            cm.setCompany(this);
        }
    }

    public void addRole(CompanyRole role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
            role.setCompany(this);
        }
    }

    public void addPeriod(SchedulePeriod p) {
        if (!this.periods.contains(p)) {
            this.periods.add(p);
            p.setCompany(this);
        }
    }

    public void addTemplate(StaffingTemplate t) {
        if (!this.templates.contains(t)) {
            this.templates.add(t);
            t.setCompany(this);
        }
    }

    public void addSchedule(Schedule s) {
        if (!this.schedules.contains(s)) {
            this.schedules.add(s);
            s.setCompany(this);
        }
    }

    public void addAvailabilitySubmission(AvailabilitySubmission sub) {
        if (!this.availabilitySubmissions.contains(sub)) {
            this.availabilitySubmissions.add(sub);
            sub.setCompany(this);
        }
    }

    public void addMemberAvailability(MemberAvailability ma) {
        if (!this.memberAvailabilities.contains(ma)) {
            this.memberAvailabilities.add(ma);
            ma.setCompany(this);
        }
    }

    public void addNotice(CompanyNotice notice) {
        if (!this.notices.contains(notice)) {
            this.notices.add(notice);
            notice.setCompany(this);
        }
    }

    public void addSwapRequest(SwapRequest swapRequest) {
        if (!this.swapRequests.contains(swapRequest)) {
            this.swapRequests.add(swapRequest);
            swapRequest.setCompany(this);
        }
    }

    //== 비즈니스 로직 ==//
    /**
     * 초대코드 적용 (초대코드는 Service 레이어에서 생성)
     */
    public void applyInviteCode(String newCode) {
        if(newCode == null || newCode.isBlank()) {
            throw new IllegalArgumentException("Invite code cannot be null or blank");
        }
        this.inviteCode = newCode;
        this.inviteCodeGeneratedAt = LocalDateTime.now();
    }


    /**
     * 회사 기본 정보 수정 (이름/주소)
     * name, addressd에 null/blank가 들어오면 그대로 유지
     */
    public void updateInfo(String name, String address) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
    }

    /**
     * 직원 제외
     */
    public void removeCompanyMember(CompanyMember cm) {
        this.companyMembers.remove(cm);
    }
}