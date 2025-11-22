package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 역할: 직원의 반복 가용시간 패턴 저장 (요일, 시간대)
 * 사용 예시: 자동 스케쥴 생성 시 가용 가능 여부 확인
 * 참고: 특정 주차 제출 여부는 availability_submissions 테이블로 관리
 */
@Entity
@Table(name = "member_availability",
        indexes = @Index(name = "idx_avail_company_member_dow", columnList = "company_id,member_id,dow"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberAvailability {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_availability_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false) private int dow; /** dow(day of week)는 요일로 0=월..6=일 */
    @Column(nullable = false) private LocalTime startTime; /** 근무 시작 시간*/
    @Column(nullable = false) private LocalTime endTime; /** 근무 종료 시간*/

    /** effectiveFrom, effectiveTo는 근무 가능 시간 유효 범위 */
    @Column(nullable = false) private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    //== 생성 메서드 ==//
    public static MemberAvailability create(Company c, Member m, int dow,
                                            LocalTime start, LocalTime end,
                                            LocalDate from, LocalDate to) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("종료시간은 시작시간 이후여야 합니다.");
        }

        MemberAvailability a = new MemberAvailability();
        a.member = m;
        a.dow = dow; a.startTime = start; a.endTime = end;
        a.effectiveFrom = from; a.effectiveTo = to;

        c.addMemberAvailability(a);
        return a;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
    }

    // == 비즈니스 메서드 == //
    public boolean isEffectiveOn(LocalDate date) {
        if (date.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && date.isAfter(effectiveTo)) return false;
        return true;
    }

    public boolean overlaps(LocalTime start, LocalTime end) {
        // startTime, endTime이랑 start, end가 겹치는지
        return !this.endTime.isBefore(start) && !this.startTime.isAfter(end);
    }
}
