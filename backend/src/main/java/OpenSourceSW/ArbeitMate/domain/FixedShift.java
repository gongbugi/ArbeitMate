package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "fixed_shifts",
        indexes = {
                @Index(name = "idx_fixed_shift_company_member_dow",
                        columnList = "company_id,member_id,dow")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FixedShift {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fixed_shift_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private CompanyRole role;


    @Column(nullable = false)
    private int dow; //dow: 0=월, 1=화, ... 6=일

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalDate effectiveFrom; // 해당 고정근무 패턴이 유효해지는 시작 날짜

    private LocalDate effectiveTo; // 유효 종료일 (null이면 무기한)

    //== 생성 메서드 ==//
    public static FixedShift create(Company company, Member member, CompanyRole role, int dow,
                                    LocalTime startTime, LocalTime endTime,
                                    LocalDate effectiveFrom, LocalDate effectiveTo) {

        if (dow < 0 || dow > 6) {
            throw new IllegalArgumentException("요일(dow)은 0~6 범위여야 합니다. (0=월..6=일)");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("유효 종료일은 시작일 이전일 수 없습니다.");
        }

        FixedShift fs = new FixedShift();
        fs.member = member;
        fs.role = role;
        fs.dow = dow;
        fs.startTime = startTime;
        fs.endTime = endTime;
        fs.effectiveFrom = effectiveFrom;
        fs.effectiveTo = effectiveTo;

        company.addFixedShift(fs); // company 연관관계 + company 세팅
        return fs;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
    }

    //== 비즈니스 로직 ==//
    /**
     * 특정 날짜에 이 고정근무 패턴이 유효한지 체크 (자동편성 단계에서 활용 예정)
     */
    public boolean isEffectiveOn(LocalDate date) {
        if (date.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && date.isAfter(effectiveTo)) return false;
        return true;
    }
}
