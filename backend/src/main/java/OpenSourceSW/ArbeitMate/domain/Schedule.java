package OpenSourceSW.ArbeitMate.domain;

import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


/**
 * 역할: 스케쥴 슬롯 (하루의 특정 시간대(근무 시간) + 역할 + 필요 인원)
 * 사용 예시: 자동 근무표 생성시, 수동 편집 시 등
 */
@Entity
@Table(name = "schedules",
        indexes = {
                @Index(name = "idx_schedules_company_date", columnList = "company_id,work_date"),
                @Index(name = "idx_schedules_period", columnList = "period_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Schedule {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "period_id", nullable = false)
    private SchedulePeriod period;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false)
    private CompanyRole role;

    @Column(nullable = false) private LocalDate workDate;
    @Column(nullable = false) private LocalTime startTime;
    @Column(nullable = false) private LocalTime endTime;
    @Column(nullable = false) private int requiredHeadcount;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleSlotAvailability> slotAvailabilities = new ArrayList<>();

    //== 생성 메서드 ==//
    public static Schedule create(Company company, SchedulePeriod period, CompanyRole role,
                                  LocalDate workDate, LocalTime startTime, LocalTime endTime,
                                  int requiredHeadcount) {
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("종료시간은 시작시간 이후여야 합니다.");
        if (requiredHeadcount <= 0) throw new IllegalArgumentException("최소 1명 이상을 배치해야합니다.");

        Schedule s = new Schedule();
        s.setCompany(company);
        s.setPeriod(period);
        s.setRole(role);
        s.workDate = workDate; s.startTime = startTime; s.endTime = endTime;
        s.requiredHeadcount = requiredHeadcount;
        return s;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) company.addSchedule(this);
    }
    public void setPeriod(SchedulePeriod period) {
        this.period = period;
        if (period != null) period.getCompany().addSchedule(this); // 회사-스케줄 연계 보장
    }
    public void setRole(CompanyRole role) { this.role = role; }

    public void addAssignment(ScheduleAssignment a) {
        if (!this.assignments.contains(a)) {
            this.assignments.add(a);
            a.setSchedule(this);
        }
    }
    public void addSlotAvailability(ScheduleSlotAvailability sa) {
        if (!this.slotAvailabilities.contains(sa)) {
            this.slotAvailabilities.add(sa);
            sa.setSchedule(this);
        }
    }

    //== 비즈니스 로직 ==//
    public boolean isFullyAssigned() {
        long assigned = assignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.ASSIGNED)
                .count();
        return assigned >= requiredHeadcount;
    }

    /**
     * 현재 requiredHeadcount 가 minRequired 보다 작으면 minRequired 까지 올리기
     * (고정 근무자 수에 맞춰 정원을 늘리는 데 사용)
     */
    public void ensureRequiredHeadcountAtLeast(int minRequired) {
        if (minRequired <= 0) {
            throw new IllegalArgumentException("requiredHeadcount는 1 이상이어야 합니다.");
        }
        if (this.requiredHeadcount < minRequired) {
            this.requiredHeadcount = minRequired;
        }
    }
}