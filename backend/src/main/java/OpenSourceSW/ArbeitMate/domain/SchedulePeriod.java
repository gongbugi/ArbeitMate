package OpenSourceSW.ArbeitMate.domain;

import OpenSourceSW.ArbeitMate.domain.enums.PeriodType;
import OpenSourceSW.ArbeitMate.domain.enums.PeriodStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 역할: 스케쥴 운영 기간 단위(주간 또는 월간) 게시 및 마감 담당
 * 사용 예시: 자동생성 실행 시점, 주/월 캘린더 범위
 */
@Entity
@Table(name = "schedule_periods",
        uniqueConstraints = @UniqueConstraint(name = "uq_period_company_name", columnNames = {"company_id","name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SchedulePeriod {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "period_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String name; // 하나의 매장에서 기간 별 다수의 스케쥴 표가 나오기에 company + name 으로 unique 설정해서 구분 (예: 2025-W46)

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PeriodType periodType; // WEEKLY, MONTHLY

    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private PeriodStatus status; // DRAFT(편성 중), PUBLISHED(편성 완료, 확정됨)

    private LocalDateTime availabilityDueAt; // 직원 가용시간 제출 마감 시간
    private LocalDateTime autoGenerateAt; // 자동 편성 실행 예정 시간 (수동으로 편성시 해당 속성은 제거 가능)
    private LocalDateTime publishedAt; // 최종 확정 계시 시간

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "published_by_member_id")
    private Member publishedBy;

    @Column(nullable = false) private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static SchedulePeriod create(Company c, String name, PeriodType type,
                                        LocalDate start, LocalDate end,
                                        LocalDateTime availabilityDueAt) {
        if(end.isBefore(start)) {
            throw new IllegalArgumentException("종료일은 시작일보다 앞설 수 없습니다.");
        }
        SchedulePeriod p = new SchedulePeriod();
        p.setCompany(c);
        p.name = name; p.periodType = type;
        p.startDate = start; p.endDate = end;
        p.availabilityDueAt = availabilityDueAt;
        p.status = PeriodStatus.DRAFT;
        return p;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) company.addPeriod(this);
    }

    //== 비즈니스 로직 ==//
    public void publish(Member publisher) {
        this.status = PeriodStatus.PUBLISHED;
        this.publishedBy = publisher;
        this.publishedAt = LocalDateTime.now();
    }
}
