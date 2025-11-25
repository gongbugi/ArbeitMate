package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyNotice;
import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.dto.request.NoticeRequest;
import OpenSourceSW.ArbeitMate.dto.response.NoticeResponse;
import OpenSourceSW.ArbeitMate.repository.CompanyNoticeRepository;
import OpenSourceSW.ArbeitMate.repository.CompanyRepository;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final CompanyNoticeRepository companyNoticeRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;

    // 공지사항 생성
    @Transactional
    public UUID createNotice(UUID memberId, UUID companyId, NoticeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));



        CompanyNotice notice = CompanyNotice.create(
                company,
                member,
                request.getTitle(),
                request.getContent()
        );

        return companyNoticeRepository.save(notice).getId();
    }

    // 매장별 공지사항 목록 조회
    public List<NoticeResponse> getCompanyNotices(UUID companyId) {
        List<CompanyNotice> notices = companyNoticeRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);

        return notices.stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    // 공지사항 상세 조회
    public NoticeResponse getNotice(UUID companyId, UUID noticeId) {
        CompanyNotice notice = companyNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        if (!notice.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 공지사항이 아닙니다.");
        }

        return new NoticeResponse(notice);
    }

    // 공지사항 삭제 (추가 기능)
    @Transactional
    public void deleteNotice(UUID companyId, UUID noticeId) {
        CompanyNotice notice = companyNoticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        if (!notice.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("해당 매장의 공지사항이 아닙니다.");
        }

        companyNoticeRepository.deleteById(noticeId);
    }
}