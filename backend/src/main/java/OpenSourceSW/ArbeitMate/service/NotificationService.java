package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.Notification;
import OpenSourceSW.ArbeitMate.dto.response.NotificationResponse;
import OpenSourceSW.ArbeitMate.repository.NotificationRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    // 👇 [추가 1] DB 작업을 위해 리포지토리 추가
    private final NotificationRepository notificationRepository;

    /**
     * [추가 2] 내 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID memberId) {
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationResponse::from) // DTO 변환
                .toList();
    }

    /**
     * [추가 3] 알림 읽음 처리
     */
    @Transactional
    public void readNotification(UUID memberId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        // 보안 검증: 내 알림이 맞는지?
        if (!notification.getReceiver().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 알림만 읽을 수 있습니다.");
        }

        notification.read(); // 읽음 상태로 변경
    }

    /**
     * [추가 4] "진짜 알림" 생성 및 발송 (다른 서비스에서 호출용)
     * DB 저장 + 핸드폰 전송을 동시에 처리합니다.
     */
    @Transactional
    public void createAndSend(Member receiver, String title, String content, String type) {
        // 1. DB 저장
        Notification notification = Notification.builder()
                .receiver(receiver)
                .title(title)
                .content(content)
                .type(type)
                .build();
        notificationRepository.save(notification);

        // 2. 핸드폰으로 푸시 알림 발송 (토큰이 있을 때만)
        // (Member 엔티티에 fcmToken 필드가 있다고 가정하거나, 없으면 이 부분은 생략 가능)
        // if (receiver.getFcmToken() != null) {
        //     sendNotification(receiver.getFcmToken(), title, content);
        // }
    }

    // =========================================================
    // 아래는 기존에 있던 RabbitMQ / Firebase 로직
    // =========================================================

    /**
     * [1] 알림 요청 (Producer)
     */
    public void sendNotification(String targetToken, String title, String body) {
        String fullMessage = targetToken + ":::" + title + ":::" + body;
        rabbitTemplate.convertAndSend("notification_queue", fullMessage);
        log.info("📤 [RabbitMQ 대기열 등록] {}", fullMessage);
    }

    /**
     * [2] 알림 실제 발송 (Consumer)
     */
    @RabbitListener(queues = "notification_queue")
    public void receiveNotification(String fullMessage) {
        try {
            String[] parts = fullMessage.split(":::", 3);
            if (parts.length < 3) return;

            String targetToken = parts[0];
            String title = parts[1];
            String body = parts[2];

            log.info("📨 [Firebase 발송 시작] To: {}", targetToken);

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ [알림 전송 성공] ID: {}", response);

        } catch (Exception e) {
            log.error("❌ [알림 전송 실패]", e);
        }
    }
}