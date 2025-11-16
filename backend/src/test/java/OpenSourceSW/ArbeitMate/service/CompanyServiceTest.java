package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.Company;
import OpenSourceSW.ArbeitMate.domain.CompanyMember;
import OpenSourceSW.ArbeitMate.domain.Member;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.UpdateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.UpdateCompanyResponse;
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

    @Test
    @DisplayName("사장인 회원이 회사 정보 수정 시 정상 동작")
    void updateCompany_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시 구로구", "INVITE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        UpdateCompanyRequest req = new UpdateCompanyRequest();
        req.setName("카페 A");
        req.setAddress("서울시 강남구");

        // when
        UpdateCompanyResponse res = companyService.updateCompany(ownerId, companyId, req);

        // then
        assertThat(company.getName()).isEqualTo("카페 A");
        assertThat(company.getAddress()).isEqualTo("서울시 강남구");
        assertThat(res.getCompanyId()).isEqualTo(companyId);
    }

    @Test
    @DisplayName("사장이 아닌 회원이 회사 정보 수정 시 예외 발생")
    void updateCompany_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID(); // 사장 아님

        Company company = newCompany("카페 A", owner, "서울시 구로구", "INVITE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        UpdateCompanyRequest req = new UpdateCompanyRequest();
        req.setName("카페 B");
        req.setAddress("경기도 수원시");

        // then
        assertThatThrownBy(() -> companyService.updateCompany(otherMemberId, companyId, req))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("사장이 아닌 회원이 초대코드 재생성 시 예외 발생")
    void regenerateInviteCode_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시 구로구", "OLD_CODE");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // then
        assertThatThrownBy(() -> companyService.regenerateInviteCode(otherMemberId, companyId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("사장이 회사 삭제 시 정상 동작")
    void deleteCompany_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시 구로구", "INVITE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        companyService.deleteCompany(ownerId, companyId);

        // then
        verify(companyRepository, times(1)).delete(company);
    }

    @Test
    @DisplayName("사장이 아닌 회원이 회사 삭제 시 예외 발생")
    void deleteCompany_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시 구로구", "INVITE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() -> companyService.deleteCompany(otherMemberId, companyId))
                .isInstanceOf(IllegalStateException.class);

        verify(companyRepository, never()).delete(any());
    }
}