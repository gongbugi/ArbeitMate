package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 역할: 매장 공지사항
 */
@Entity
@Table(
        name = "company_notices",
        indexes = {
                @Index(name = "idx_notice_company_created",   columnList = "company_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CompanyNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notice_id", updatable = false, nullable = false)
    private UUID id;

    /** 공지 소속 매장 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** 작성자(멤버) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_member_id", nullable = false)
    private Member createdBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    /** 생성 시각(영속화 기준) */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ===== 라이프사이클 =====
    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ===== 생성 메서드 =====
    public static CompanyNotice create(Company company, Member author, String title, String content) {
        CompanyNotice n = new CompanyNotice();
        n.createdBy = author;
        n.title = title;
        n.content = content;

        company.addNotice(n);
        return n;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
    }

    // ===== 비즈니스 로직 =====
    /**
     * 수정
     */
    public void update(String newTitle, String newContent) {
        if (newTitle == null || newTitle.isBlank())     throw new IllegalArgumentException("title must not be blank");
        if (newContent == null || newContent.isBlank()) throw new IllegalArgumentException("content must not be blank");
        this.title = newTitle;
        this.content = newContent;
    }
}
