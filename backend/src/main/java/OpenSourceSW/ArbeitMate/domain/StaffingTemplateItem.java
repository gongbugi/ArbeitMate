package OpenSourceSW.ArbeitMate.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * 역할: 템플릿의 구체 슬롯(요일, 시간, 역할, 필요 인원)
 * 사용 예시: 템플릿에서의 실제 스케쥴 슬롯으로 일괄 생성 시
 */
@Entity @Table(name = "staffing_template_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class StaffingTemplateItem {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_item_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "template_id", nullable = false)
    private StaffingTemplate template;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", nullable = false)
    private CompanyRole role;

    @Column(nullable = false) private int dow; // day of week로 요일 표시 (0:월요일 ~ 6: 일요일)
    @Column(nullable = false) private LocalTime startTime;
    @Column(nullable = false) private LocalTime endTime;
    @Column(nullable = false) private int headcount;

    //== 생성 메서드 ==//
    public static StaffingTemplateItem create(StaffingTemplate t, CompanyRole role,
                                              int dow, LocalTime start, LocalTime end, int headcount) {
        StaffingTemplateItem i = new StaffingTemplateItem();
        i.setTemplate(t);
        i.setRole(role);
        i.dow = dow; i.startTime = start; i.endTime = end; i.headcount = headcount;
        return i;
    }

    //== 연관관계 편의 메서드 ==//
    public void setTemplate(StaffingTemplate template) {
        this.template = template;
        if (template != null) template.addItem(this);
    }

    public void setRole(CompanyRole role) {
        this.role = role;
    }
}