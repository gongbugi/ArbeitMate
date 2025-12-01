package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.Notification;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String content;
    private boolean isRead;
    private String type;
    private LocalDateTime createdAt;

    // 엔티티 -> DTO 변환 메소드
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(n.isRead())
                .type(n.getType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}