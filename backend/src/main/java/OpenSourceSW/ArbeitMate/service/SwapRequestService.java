package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.*;
import OpenSourceSW.ArbeitMate.domain.enums.SwapType;
import OpenSourceSW.ArbeitMate.dto.request.CreateSwapRequest;
import OpenSourceSW.ArbeitMate.dto.response.SwapRequestResponse;
import OpenSourceSW.ArbeitMate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;
    private final MemberRepository memberRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;
    private final CompanyRepository companyRepository;

    // 알림 서비스를 주입받습니다.
    private final NotificationService notificationService;

    /**
     * 1. 근무 교환/대타 요청 생성 (알바생)
     */
    @Transactional
    public UUID createRequest(UUID requesterId, UUID companyId, CreateSwapRequest req) {
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        // 내 근무(From) 조회
        ScheduleAssignment fromAssignment = scheduleAssignmentRepository.findById(req.getFromAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("내 근무 정보를 찾을 수 없습니다."));

        if (!fromAssignment.getMember().getId().equals(requesterId)) {
            throw new IllegalArgumentException("본인의 근무만 교환 신청할 수 있습니다.");
        }

        // 대상 근무(To) 조회
        ScheduleAssignment toAssignment = null;
        if (req.getType() == SwapType.DIRECT_SWAP) {
            if (req.getToAssignmentId() == null) {
                throw new IllegalArgumentException("맞교환 시 상대방의 근무 정보가 필요합니다.");
            }
            toAssignment = scheduleAssignmentRepository.findById(req.getToAssignmentId())
                    .orElseThrow(() -> new IllegalArgumentException("상대방 근무 정보를 찾을 수 없습니다."));
        }

        // 특정 대상 지정
        Member targetMember = null;
        if (req.getTargetMemberId() != null) {
            targetMember = memberRepository.findById(req.getTargetMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));
        }

        // 엔티티 생성
        SwapRequest swapRequest;
        if (req.getType() == SwapType.GIVE_AWAY) {
            if (targetMember == null) {
                swapRequest = SwapRequest.createGiveAwayOpen(company, fromAssignment, requester);
            } else {
                swapRequest = SwapRequest.createGiveAway(company, fromAssignment, requester, targetMember);
            }
        } else {
            if (targetMember == null) {
                targetMember = toAssignment.getMember();
            }
            swapRequest = SwapRequest.createDirectSwap(company, fromAssignment, toAssignment, requester, targetMember);
        }

        swapRequestRepository.save(swapRequest);

        // 특정 대상에게 알림 발송 (저장+전송)
        if (targetMember != null) {
            String notiTitle = (req.getType() == SwapType.GIVE_AWAY) ? "대타 요청" : "근무 교환 요청";
            String notiBody = requester.getName() + "님이 근무 변경을 요청했습니다.";

            notificationService.createAndSend(targetMember, notiTitle, notiBody, "SHIFT_CHANGE");
        }

        return swapRequest.getId();
    }

    /**
     * 2. 요청 수락 (대상 알바생)
     */
    @Transactional
    public void acceptRequest(UUID memberId, UUID requestId) {
        Member accepter = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        SwapRequest request = swapRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 정보를 찾을 수 없습니다."));

        request.accept(accepter);
        request.requestOwnerApproval();

        // 사장님에게 승인 요청 알림 발송
        Member owner = request.getCompany().getOwner(); // 사장님 찾기
        notificationService.createAndSend(
                owner,
                "근무 변경 승인 요청",
                accepter.getName() + "님이 근무 변경 요청을 수락했습니다. 승인해주세요.",
                "SHIFT_CHANGE"
        );
    }

    /**
     * 3. 최종 승인 (사장님) -> ★ 실제 스케줄 변경 발생 ★
     */
    @Transactional
    public void approveRequest(UUID ownerId, UUID requestId) {
        Member owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        SwapRequest request = swapRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 정보를 찾을 수 없습니다."));

        if (!request.getCompany().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("해당 매장의 사장님만 승인할 수 있습니다.");
        }

        request.approve(owner);
        updateScheduleAssignments(request);

        // 요청자(A)와 수락자(B) 모두에게 완료 알림 발송
        String message = "근무 변경 요청이 사장님에 의해 승인되었습니다.";

        // A에게 알림
        notificationService.createAndSend(request.getCreatedBy(), "근무 변경 승인", message, "SHIFT_CHANGE");

        // B에게 알림 (A랑 B가 다를 때만)
        if (request.getAcceptedMember() != null && !request.getCreatedBy().equals(request.getAcceptedMember())) {
            notificationService.createAndSend(request.getAcceptedMember(), "근무 변경 승인", message, "SHIFT_CHANGE");
        }
    }

    /**
     * 실제 근무표 변경 로직
     */
    private void updateScheduleAssignments(SwapRequest request) {
        ScheduleAssignment from = request.getFromAssignment();
        Member requester = request.getCreatedBy();
        Member newWorker = request.getAcceptedMember();

        if (request.getType() == SwapType.GIVE_AWAY) {
            from.setMember(newWorker);
        } else if (request.getType() == SwapType.DIRECT_SWAP) {
            ScheduleAssignment to = request.getToAssignment();
            from.setMember(newWorker);
            to.setMember(requester);
        }
    }

    /**
     * 4. 거절 (대상자 또는 사장님)
     */
    @Transactional
    public void declineRequest(UUID memberId, UUID requestId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        SwapRequest request = swapRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 정보를 찾을 수 없습니다."));

        request.decline(member);

        // 요청자에게 거절 알림 발송
        // 거절한 사람이 본인이 아닐 때만 알림 (내가 취소한 거면 알림 X)
        if (!request.getCreatedBy().getId().equals(memberId)) {
            notificationService.createAndSend(
                    request.getCreatedBy(),
                    "근무 변경 거절",
                    member.getName() + "님이 요청을 거절했습니다.",
                    "SHIFT_CHANGE"
            );
        }
    }

    /**
     * 조회: 알바생용 (내 관련 요청)
     */
    public List<SwapRequestResponse> getMyRequests(UUID memberId) {
        return swapRequestRepository.findAllMyRelatedRequests(memberId).stream()
                .map(SwapRequestResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 조회: 사장님용 (매장 전체 요청)
     */
    public List<SwapRequestResponse> getCompanyRequests(UUID companyId) {
        return swapRequestRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(SwapRequestResponse::new)
                .collect(Collectors.toList());
    }
}