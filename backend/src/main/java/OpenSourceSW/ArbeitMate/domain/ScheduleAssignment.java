package OpenSourceSW.ArbeitMate.domain;

import OpenSourceSW.ArbeitMate.domain.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 역할: 특정 스케쥴 슬롯에 실제 직원 배정
 * 사용 예시: 배정 확정/교체/취소, 급여 계산에 활용
 */
@Entity
@Table(name = "schedule_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uq_assignment_unique", columnNames = {"schedule_id","member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ScheduleAssignment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_assignment_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AssignmentStatus status;

    //== 생성 메서드 ==//
    public static ScheduleAssignment create(Schedule s, Member m) {
        ScheduleAssignment a = new ScheduleAssignment();
        a.setSchedule(s);
        a.setMember(m);
        a.status = AssignmentStatus.ASSIGNED;
        return a;
    }

    //== 연관관계 편의 메서드 ==//
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
        if (schedule != null) schedule.addAssignment(this);
    }
    public void setMember(Member member) {
        this.member = member;
    }

    //== 비즈니스 로직 ==//
    public void assignTo(Member newMember) {
        this.member = newMember;
        this.status = AssignmentStatus.ASSIGNED;
    }

    public void markPendingSwap() {
        this.status = AssignmentStatus.PENDING_SWAP;
    }
    public void cancel() {
        this.status = AssignmentStatus.CANCELLED;
    }
}
