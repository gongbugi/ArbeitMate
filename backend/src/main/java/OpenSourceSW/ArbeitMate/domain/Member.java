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
 * 역할: 앱 사용자 게정 정보
 * 사용 예시: 회원가입/로그인 등
 */
@Entity @Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String password; // 해시된 값 저장
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String phone;

    @Column(nullable = false) private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMember> companyMemberships = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static Member create(String email, String password, String name, String phone) {
        Member m = new Member();
        m.email = email;
        m.password = password;
        m.name = name;
        m.phone = phone;
        return m;
    }

    //== 연관관계 편의 메서드 ==//
    public void addMembership(CompanyMember cm) {
        if (!this.companyMemberships.contains(cm)) {
            this.companyMemberships.add(cm);
            cm.setMember(this);
        }
    }
}
