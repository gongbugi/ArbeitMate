package OpenSourceSW.ArbeitMate.domain;

import OpenSourceSW.ArbeitMate.domain.enums.SwapType;
import OpenSourceSW.ArbeitMate.domain.enums.SwapStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 역할: 대타/근무 교환 요청 관리
 */
@Entity @Table(name = "swap_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SwapRequest {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "swap_request_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "from_assignment_id", nullable = false)
    private ScheduleAssignment fromAssignment;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "to_assignment_id")
    private ScheduleAssignment toAssignment; // null일 경우 서로간의 교체가 아닌 다른 사람이 대체 근무

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proposed_to_member_id")
    private Member proposedTo; // null = 공개 요청, 특정 대상에게 보낼 경우에는 설정

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private SwapType type; // GIVE_AWAY, DIRECT_SWAP

    /**
     * 순서: 요청 생성(OPEN) -> 수락(ACCEPTED)  -> 사장 승인대기(OWNER_APPROVAL) -> 최종 승인(APPROVED)
     *                     -> 거절(DECLINED) / 취소(CANCELED)
     */
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private SwapStatus status; // OPEN, ACCEPTED, OWNER_APPROVAL, APPROVED, DECLINED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "accepted_member_id")
    private Member acceptedMember;
    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_member_id", nullable = false)
    private Member createdBy;
    @Column(nullable = false) private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by_member_id")
    private Member approvedBy;
    private LocalDateTime approvedAt;

    /** 동시 수락 경합 방지(낙관적 락) */
    @Version
    private long version;

    // ===== 라이프사이클 =====
    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        validateShape();
        if (status == null) status = SwapStatus.OPEN;
    }

    //== 생성 메서드 ==//
    /** 대타 공개 요청(전체 대상) */
    public static SwapRequest createGiveAwayOpen(Company company, ScheduleAssignment from,
                                                 Member requester) {
        SwapRequest r = new SwapRequest();
        r.company = company;
        r.fromAssignment = from;
        r.toAssignment = null;
        r.type = SwapType.GIVE_AWAY;
        r.status = SwapStatus.OPEN;
        r.createdBy = requester;
        r.proposedTo = null; // 공개 요청 (전체 대상)
        return r;
    }

    /** 대타 요청(특정 1인) */
    public static SwapRequest createGiveAway(Company company, ScheduleAssignment from,
                                             Member requester, Member proposedTo) {
        SwapRequest r = new SwapRequest();
        r.company = company;
        r.fromAssignment = from;
        r.toAssignment = null;
        r.type = SwapType.GIVE_AWAY;
        r.status = SwapStatus.OPEN;
        r.createdBy = requester;
        r.proposedTo = proposedTo;
        return r;
    }

    /** 직접 교환 요청(특정 1인)*/
    public static SwapRequest createDirectSwap(Company company, ScheduleAssignment from, ScheduleAssignment to,
                                               Member requester, Member proposedTo) {
        SwapRequest r = new SwapRequest();
        r.company = company;
        r.fromAssignment = from;
        r.toAssignment = to;
        r.type = SwapType.DIRECT_SWAP;
        r.status = SwapStatus.OPEN;
        r.createdBy = requester;
        r.proposedTo = proposedTo;
        return r;
    }

    // ===== 조회/검증 헬퍼 =====
    public boolean isBroadcast() { return this.proposedTo == null; }
    public boolean isOpen() { return this.status == SwapStatus.OPEN; }

    /** 엔티티 형태 검증: 타입과 toAssignment의 일관성 목적 */
    private void validateShape() {
        if (type == SwapType.GIVE_AWAY && toAssignment != null) {
            throw new IllegalStateException("GIVE_AWAY must have null toAssignment");
        }
        if (type == SwapType.DIRECT_SWAP && toAssignment == null) {
            throw new IllegalStateException("DIRECT_SWAP requires toAssignment");
        }
    }

    /** 이 멤버가 수락 가능한 대상인지(도메인 수준 1차 검증) */
    private boolean isAcceptableBy(Member m) {
        if (!isOpen()) return false;

        if (type == SwapType.GIVE_AWAY) {
            // 지정요청: 해당 1인만
            if (proposedTo != null) {
                return proposedTo.getId().equals(m.getId());
            }
            // 공개요청: 도메인 수준에서는 통과(역할/가용성 자격은 서비스에서 추가 검사)
            return true;
        }

        // DIRECT_SWAP: 기본은 toAssignment의 담당자만 가능
        UUID assigneeId = toAssignment.getMember().getId();
        if (proposedTo != null) {
            // 명시 지정이 있으면 그 사람만(보통 toAssignment 담당자와 동일해야 자연스럽지만, 정책에 따라 강제 가능)
            return proposedTo.getId().equals(m.getId());
        }
        return assigneeId.equals(m.getId());
    }

    //== 비즈니스 로직 ==//
    /**
     * 수락(선착순 1명 보장: @Version으로 경합 방지, 서비스/리포지토리에서 예외 처리)
     */
    public void accept(Member accepter) {
        if (!isAcceptableBy(accepter)) {
            throw new IllegalStateException("Not eligible to accept this request");
        }
        this.acceptedMember = accepter;
        this.acceptedAt = LocalDateTime.now();
        this.status = SwapStatus.ACCEPTED;
    }

    public void requestOwnerApproval() {
        if (this.status != SwapStatus.ACCEPTED) throw new IllegalStateException("Not accepted");
        this.status = SwapStatus.OWNER_APPROVAL;
    }

    public void approve(Member approver) {
        if (this.status != SwapStatus.OWNER_APPROVAL) throw new IllegalStateException("Not in owner approval");
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.status = SwapStatus.APPROVED;
    }

    public void decline(Member approver) {
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.status = SwapStatus.DECLINED;
    }

    public void cancel(Member requester) {
        if (!this.createdBy.getId().equals(requester.getId()))
            throw new IllegalStateException("Only requester can cancel");
        if (this.status == SwapStatus.APPROVED)
            throw new IllegalStateException("Already approved");
        this.status = SwapStatus.CANCELLED;
    }
}