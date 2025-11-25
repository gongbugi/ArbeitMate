package OpenSourceSW.ArbeitMate.dto.response;

import OpenSourceSW.ArbeitMate.domain.CompanyNotice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponse {
    private String id;
    private String title;
    private String content;
    private String writerName;
    private LocalDateTime createdAt;

    public NoticeResponse(CompanyNotice notice) {
        this.id = notice.getId().toString();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.writerName = notice.getCreatedBy().getName();
        this.createdAt = notice.getCreatedAt();
    }

    public static NoticeResponse from(CompanyNotice notice) {
        return new NoticeResponse(notice);
    }
}