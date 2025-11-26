package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 발송 테스트
    // POST /notifications/send?token={폰토큰}&title={제목}&body={내용}
    @PostMapping("/send")
    public ResponseEntity<String> sendTest(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body) {

        notificationService.sendNotification(token, title, body);
        return ResponseEntity.ok("RabbitMQ 큐에 등록 완료 (잠시 후 핸드폰으로 알림이 갑니다)");
    }
}