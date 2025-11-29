package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 역할: 특정 슬롯에 대해 직원이 가능 여부 확인
 * 사용 예시: 대타 요청시 해당 시간 가능한 사람들 표시할 때
 * 참고: member_availability 는 패턴(상시), schedule_slot_availability 는 해당 기간의 실제 슬롯 단위 응답
 */
@Entity
@Table(name = "schedule_slot_availability",
        uniqueConstraints = @UniqueConstraint(name = "uq_slot_avail", columnNames = {"schedule_id","member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScheduleSlotAvailability {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_availability_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false) private boolean willing; // 해당 시간 가능 여부
    @Column(nullable = false) private LocalDateTime submittedAt;

    //== 생성 메서드 ==//
    public static ScheduleSlotAvailability willing(Schedule s, Member m) {
        ScheduleSlotAvailability x = new ScheduleSlotAvailability();
        //x.setSchedule(s);
        x.schedule = s;
        x.member = m;
        x.willing = true;
        x.submittedAt = LocalDateTime.now();
        return x;
    }

    //== 연관관계 편의 메서드 ==//
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        // if (schedule != null) schedule.addSlotAvailability(this);
    }
    public void setMember(Member member) { this.member = member; }
}
