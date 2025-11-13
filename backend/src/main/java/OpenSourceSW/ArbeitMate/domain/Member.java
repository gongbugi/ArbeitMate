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
    @Column(name="firebase_uid", unique = true) private String firebaseUid;

    @Column(nullable = false) private String name;

    @Column(nullable = false) private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMember> companyMemberships = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    //== 생성 메서드 ==//
    public static Member create(String email, String name) {
        Member m = new Member();
        m.email = email;
        m.name = name;
        return m;
    }

    //== 연관관계 편의 메서드 ==//
    public void addMembership(CompanyMember cm) {
        if (!this.companyMemberships.contains(cm)) {
            this.companyMemberships.add(cm);
            cm.setMember(this);
        }
    }

    //== 비즈니스 로직==//
    public void linkFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public void updateProfile(String name) {
        if(name != null && !name.isBlank()) this.name = name;
    }
}
