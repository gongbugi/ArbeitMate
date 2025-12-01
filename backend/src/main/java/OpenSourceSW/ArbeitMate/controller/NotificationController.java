package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.response.NotificationResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회 (최신순)
     * GET /notifications/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal AuthPrincipal principal) {

        List<NotificationResponse> responses = notificationService.getMyNotifications(principal.memberId());
        return ResponseEntity.ok(responses);
    }

    /**
     * 알림 읽음 처리
     * PATCH /notifications/{notificationId}/read
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID notificationId) {

        notificationService.readNotification(principal.memberId(), notificationId);
        return ResponseEntity.ok().build();
    }

    // (기존) 알림 발송 테스트
    @PostMapping("/send")
    public ResponseEntity<String> sendTest(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body) {

        notificationService.sendNotification(token, title, body);
        return ResponseEntity.ok("RabbitMQ 큐에 등록 완료");
    }
}