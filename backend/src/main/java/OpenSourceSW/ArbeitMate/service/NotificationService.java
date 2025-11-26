package OpenSourceSW.ArbeitMate.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * [1] 알림 요청 (Producer)
     * targetToken: 사용자의 FCM 토큰 (핸드폰 고유 주소)
     */
    public void sendNotification(String targetToken, String title, String body) {
        // 메시지 구분자 사용 (토큰:제목:내용)
        String fullMessage = targetToken + ":::" + title + ":::" + body;
        rabbitTemplate.convertAndSend("notification_queue", fullMessage);
        log.info("📤 [RabbitMQ 대기열 등록] {}", fullMessage);
    }

    /**
     * [2] 알림 실제 발송 (Consumer)
     * RabbitMQ에서 하나씩 꺼내서 Firebase를 통해 핸드폰으로 전송
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
                    .setToken(targetToken) // 받는 사람 폰 토큰
                    .setNotification(Notification.builder()
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