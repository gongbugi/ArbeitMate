package OpenSourceSW.ArbeitMate.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("알림 전송 요청 - RabbitMQ 큐에 올바른 형식으로 메시지가 등록되어야 함")
    void sendNotification_success() {
        // given (준비)
        String targetToken = "fake_device_token_123";
        String title = "대타 요청";
        String body = "홍길동님이 대타를 요청했습니다.";

        String expectedMessage = "fake_device_token_123:::대타 요청:::홍길동님이 대타를 요청했습니다.";

        // when (실행)
        notificationService.sendNotification(targetToken, title, body);

        // then (검증)
        // 1. rabbitTemplate.convertAndSend() 메소드가 정확히 "notification_queue"로 호출되었는지 확인
        // 2. 메시지 내용이 ":::" 으로 잘 합쳐져서 나갔는지 확인
        verify(rabbitTemplate, times(1))
                .convertAndSend("notification_queue", expectedMessage);
    }
}