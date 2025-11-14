package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.repository.CompanyRepository;
import OpenSourceSW.ArbeitMate.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock CompanyRepository companyRepository;

    @InjectMocks CompanyService companyService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(companyService, "currentHourlyWage", 10030);
    }

    /**
     * 테스트용 Member 생성 헬퍼
     */
    private Member newMember(String email, String name) {
        Member m = Member.create(email, name);
        return m;
    }

    private Company newCompany(String name, Member owner, String address, String inviteCode) {
        return Company.create(name, owner, address, inviteCode);
    }

    @Test
    @DisplayName("이미 가입된 회원은 다시 참가요청시 예외 발생")
    public void participateCompany_duplicateJoin() throws Exception {
        // given
        UUID workerId = UUID.randomUUID();
        Member worker = newMember("worker@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", workerId);

        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());

        String inviteCode = "ABCD1234";
        Company company = newCompany("카페 A", owner, "서울시 ~~", inviteCode);

        // 리포지토리 mock 설정
        when(memberRepository.findById(workerId)).thenReturn(Optional.of(worker));
        when(companyRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(company));

        ParticipateCompanyRequest req = new ParticipateCompanyRequest();
        req.setInviteCode(inviteCode);

        // when
        // 이미 worker가 이 company에 가입되어 있는 상태
        CompanyMember existing = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);

        // then
        assertThatThrownBy(() -> companyService.participateCompany(workerId, req))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("아직 가입되지 않은 회원은 정상 참가")
    public void participateCompany_success() throws Exception {
        // given
        UUID workerId = UUID.randomUUID();
        Member worker = newMember("worker2@test.com", "Worker2");
        ReflectionTestUtils.setField(worker, "id", workerId);

        Member owner = newMember("owner2@test.com", "Owner2");
        ReflectionTestUtils.setField(owner, "id", UUID.randomUUID());

        String inviteCode = "BBBB1234";
        Company company = newCompany("분식집 B", owner, "경기도 수원시 ~~", inviteCode);

        // 아직 company에는 worker에 대한 CompanyMember가 없음

        when(memberRepository.findById(workerId)).thenReturn(Optional.of(worker));
        when(companyRepository.findByInviteCode(inviteCode)).thenReturn(Optional.of(company));

        ParticipateCompanyRequest req = new ParticipateCompanyRequest();
        req.setInviteCode(inviteCode);

        // when
        var res = companyService.participateCompany(workerId, req);

        // then
        assertThat(res.getCompanyId()).isEqualTo(company.getId());
    }
}