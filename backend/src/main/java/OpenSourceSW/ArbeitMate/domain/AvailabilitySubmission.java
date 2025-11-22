package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 역할: 특정 편성 기간(주/월)에 대해 가용시간 제출 ‘했는지’ 여부 확인
 * 사용 예시: “가용시간 제출 현황” 화면, 마감 체크
 */
@Entity
@Table(name = "availability_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_avail_submit", columnNames = {"company_id","period_id","member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AvailabilitySubmission {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "availability_submission_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "period_id", nullable = false)
    private SchedulePeriod period;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false) private LocalDateTime submittedAt;

    //== 생성 메서드 ==//
    public static AvailabilitySubmission create(Company c, SchedulePeriod p, Member m) {
        AvailabilitySubmission s = new AvailabilitySubmission();
        s.period = p;
        s.member = m;
        s.submittedAt = LocalDateTime.now();

        c.addAvailabilitySubmission(s);
        return s;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
    }

    //== 비즈니스 메서드==//
    public void refreshSubmittedAt() {
        this.submittedAt = LocalDateTime.now();
    }
}
