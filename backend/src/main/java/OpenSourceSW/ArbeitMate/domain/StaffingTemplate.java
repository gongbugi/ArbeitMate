package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 역할: 필요 인원 템플릿 헤더(이름, 생성자, 생성 시각)
 * 사용 예시: 다음 주/달에 재사용할 기본 근무 슬롯 세트 저장
 */
@Entity
@Table(name = "staffing_templates",
        uniqueConstraints = @UniqueConstraint(name = "uq_template_company_name", columnNames = {"company_id","name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StaffingTemplate {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false) private String name;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private Member createdBy;

    @Column(nullable = false) private LocalDateTime createdAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffingTemplateItem> items = new ArrayList<>();

    @PrePersist
    private void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    //== 생성 메서드 ==//
    public static StaffingTemplate create(Company c, String name, Member createdBy) {
        StaffingTemplate t = new StaffingTemplate();
        t.setCompany(c);
        t.name = name; t.createdBy = createdBy;
        return t;
    }

    //== 연관관계 편의 메서드 ==//
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) company.addTemplate(this);
    }

    public void addItem(StaffingTemplateItem item) {
        if (!this.items.contains(item)) {
            this.items.add(item);
            item.setTemplate(this);
        }
    }
}
