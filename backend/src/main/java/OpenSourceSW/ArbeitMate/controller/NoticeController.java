package OpenSourceSW.ArbeitMate.controller;

import OpenSourceSW.ArbeitMate.dto.request.NoticeRequest;
import OpenSourceSW.ArbeitMate.dto.response.NoticeResponse;
import OpenSourceSW.ArbeitMate.security.AuthPrincipal;
import OpenSourceSW.ArbeitMate.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 등록
    // POST /companies/{companyId}/notices
    @PostMapping
    public ResponseEntity<String> createNotice(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID companyId,
            @RequestBody NoticeRequest request) {

        UUID noticeId = noticeService.createNotice(principal.memberId(), companyId, request);
        return ResponseEntity.ok("공지사항 등록 완료: " + noticeId);
    }

    // 공지사항 목록 조회
    // GET /companies/{companyId}/notices
    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getCompanyNotices(@PathVariable UUID companyId) {
        return ResponseEntity.ok(noticeService.getCompanyNotices(companyId));
    }

    // 공지사항 상세 조회
    // GET /companies/{companyId}/notices/{noticeId}
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(
            @PathVariable UUID companyId,
            @PathVariable UUID noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(companyId, noticeId));
    }

    // 공지사항 삭제
    // DELETE /companies/{companyId}/notices/{noticeId}
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<String> deleteNotice(
            @PathVariable UUID companyId,
            @PathVariable UUID noticeId) {
        noticeService.deleteNotice(companyId, noticeId);
        return ResponseEntity.ok("공지사항 삭제 완료");
    }
}