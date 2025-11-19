package OpenSourceSW.ArbeitMate.service;

import OpenSourceSW.ArbeitMate.domain.*;
import OpenSourceSW.ArbeitMate.domain.enums.MembershipRole;
import OpenSourceSW.ArbeitMate.dto.request.CreateRoleRequest;
import OpenSourceSW.ArbeitMate.dto.request.ParticipateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.request.UpdateCompanyRequest;
import OpenSourceSW.ArbeitMate.dto.response.CompanyRoleResponse;
import OpenSourceSW.ArbeitMate.dto.response.CompanyWorkerResponse;
import OpenSourceSW.ArbeitMate.dto.response.UpdateCompanyResponse;
import OpenSourceSW.ArbeitMate.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock CompanyRepository companyRepository;
    @Mock CompanyRoleRepository companyRoleRepository;
    @Mock CompanyMemberRoleRepository companyMemberRoleRepository;
    @Mock AvailabilitySubmissionRepository availabilitySubmissionRepository;

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

    @Test
    @DisplayName("사장은 매장 직원 목록(사장 제외)을 조회가능")
    void listWorkers_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker1 = newMember("w1@test.com", "Worker1");
        ReflectionTestUtils.setField(worker1, "id", UUID.randomUUID());

        Member worker2 = newMember("w2@test.com", "Worker2");
        ReflectionTestUtils.setField(worker2, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        // 사장 + 알바 두 명 등록
        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);
        CompanyMember cm1 = CompanyMember.create(company, worker1, MembershipRole.WORKER, 10030);
        CompanyMember cm2 = CompanyMember.create(company, worker2, MembershipRole.WORKER, 10030);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        List<CompanyWorkerResponse> workers = companyService.listWorkers(ownerId, companyId);

        // then: 사장은 제외, 알바 2명만
        assertThat(workers).hasSize(2);
        assertThat(workers)
                .extracting("name")
                .containsExactlyInAnyOrder("Worker1", "Worker2");
    }

    @Test
    @DisplayName("사장이 아닌 회원은 직원 목록을 조회 시 예외 발생")
    void listWorkers_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);
        CompanyMember cm = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() -> companyService.listWorkers(worker.getId(), companyId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("사장은 매장에서 알바생을 제외할 수 있고, 관련 제출 이력도 삭제")
    void removeWorker_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);
        CompanyMember workerCm = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);
        UUID workerCmId = UUID.randomUUID();
        ReflectionTestUtils.setField(ownerCm, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(workerCm, "id", workerCmId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        companyService.removeWorker(ownerId, companyId, workerCmId);

        // then: companyMembers 에서 제거되었는지
        assertThat(company.getCompanyMembers())
                .noneMatch(cm -> cm.getId().equals(workerCmId));

        // availability_submission 삭제 호출되었는지
        verify(availabilitySubmissionRepository, times(1))
                .deleteByCompanyAndMember(company, worker);
    }

    @Test
    @DisplayName("사장은 자신의 CompanyMember(OWNER)는 삭제 시 예외 발생")
    void removeWorker_ownerCompanyMember_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);
        UUID ownerCmId = UUID.randomUUID();
        ReflectionTestUtils.setField(ownerCm, "id", ownerCmId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() -> companyService.removeWorker(ownerId, companyId, ownerCmId))
                .isInstanceOf(IllegalStateException.class);

        verify(availabilitySubmissionRepository, never()).deleteByCompanyAndMember(any(), any());
    }

    @Test
    @DisplayName("사장은 회사에 새로운 역할군을 추가 가능")
    void createRole_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRoleRepository.existsByCompanyIdAndName(companyId, "홀")).thenReturn(false);

        CreateRoleRequest req = new CreateRoleRequest();
        req.setName("홀");

        // when
        CompanyRoleResponse res = companyService.createRole(ownerId, companyId, req);

        // then
        assertThat(res.getName()).isEqualTo("홀");
        assertThat(company.getRoles())
                .extracting("name")
                .contains("홀");
        verify(companyRoleRepository, times(1)).save(any(CompanyRole.class));
    }

    @Test
    @DisplayName("이미 존재하는 역할 이름으로 추가하려 하면 예외 발생")
    void createRole_duplicateName_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRoleRepository.existsByCompanyIdAndName(companyId, "홀")).thenReturn(true);

        CreateRoleRequest req = new CreateRoleRequest();
        req.setName("홀");

        // when & then
        assertThatThrownBy(() -> companyService.createRole(ownerId, companyId, req))
                .isInstanceOf(IllegalStateException.class);

        verify(companyRoleRepository, never()).save(any());
    }

    @Test
    @DisplayName("매장 소속인 멤버는 역할군 목록을 조회 가능")
    void listRoles_member_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        // 회사에 역할 2개 추가
        CompanyRole r1 = CompanyRole.create(company, "홀");
        CompanyRole r2 = CompanyRole.create(company, "주방");

        // worker를 companyMembers 에 추가
        CompanyMember cm = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        List<CompanyRoleResponse> roles = companyService.listRoles(worker.getId(), companyId);

        // then
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting("name")
                .containsExactlyInAnyOrder("홀", "주방");
    }

    @Test
    @DisplayName("매장에 속하지 않은 멤버는 역할군 목록 조회 시 예외 발생")
    void listRoles_nonMember_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member outsider = newMember("out@test.com", "Outsider");
        ReflectionTestUtils.setField(outsider, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        // companyMembers에는 owner만 있다고 가정
        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when & then
        assertThatThrownBy(() -> companyService.listRoles(outsider.getId(), companyId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("사장은 회사의 특정 직원에게 역할군을 부여 가능")
    void assignRoleToWorker_owner_success() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        // companyMembers: owner + worker
        CompanyMember ownerCm = CompanyMember.create(company, owner, MembershipRole.OWNER, 10030);
        CompanyMember workerCm = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);
        UUID workerCmId = UUID.randomUUID();
        ReflectionTestUtils.setField(ownerCm, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(workerCm, "id", workerCmId);

        // 역할 생성
        CompanyRole role = CompanyRole.create(company, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);

        List<UUID> roleIds = List.of(roleId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRoleRepository.findAllById(roleIds)).thenReturn(List.of(role));
        when(companyMemberRoleRepository.existsByCompanyAndMemberAndRole(company, worker, role))
                .thenReturn(false);

        // when
        companyService.assignRoleToWorker(ownerId, companyId, workerCmId, roleIds);

        // then
        verify(companyMemberRoleRepository, times(1))
                .save(any(CompanyMemberRole.class));
    }

    @Test
    @DisplayName("사장이 아닌 회원은 직원에게 역할군을 부여할 시 예외 발생")
    void assignRoleToWorker_nonOwner_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        UUID otherMemberId = UUID.randomUUID();

        Company company = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId = UUID.randomUUID();
        ReflectionTestUtils.setField(company, "id", companyId);

        CompanyMember workerCm = CompanyMember.create(company, worker, MembershipRole.WORKER, 10030);
        UUID workerCmId = UUID.randomUUID();
        ReflectionTestUtils.setField(workerCm, "id", workerCmId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        List<UUID> roleIds = new ArrayList<>();
        roleIds.add(UUID.randomUUID());

        // when & then
        assertThatThrownBy(() -> companyService.assignRoleToWorker(otherMemberId, companyId, workerCmId, roleIds))
                .isInstanceOf(IllegalStateException.class);

        verify(companyMemberRoleRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 매장의 역할군을 부여하려고 하면 예외 발생")
    void assignRoleToWorker_roleNotFromCompany_throws() {
        // given
        UUID ownerId = UUID.randomUUID();
        Member owner = newMember("owner@test.com", "Owner");
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Member worker = newMember("w@test.com", "Worker");
        ReflectionTestUtils.setField(worker, "id", UUID.randomUUID());

        Company company1 = newCompany("카페 A", owner, "서울시", "CODE1");
        UUID companyId1 = UUID.randomUUID();
        ReflectionTestUtils.setField(company1, "id", companyId1);

        Company company2 = newCompany("카페 B", owner, "서울시", "CODE2");
        UUID companyId2 = UUID.randomUUID();
        ReflectionTestUtils.setField(company2, "id", companyId2);

        CompanyMember workerCm = CompanyMember.create(company1, worker, MembershipRole.WORKER, 10030);
        UUID workerCmId = UUID.randomUUID();
        ReflectionTestUtils.setField(workerCm, "id", workerCmId);

        // role 은 company2 소속
        CompanyRole otherRole = CompanyRole.create(company2, "홀");
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(otherRole, "id", roleId);
        List<UUID> roleIds = List.of(roleId);

        when(companyRepository.findById(companyId1)).thenReturn(Optional.of(company1));
        when(companyRoleRepository.findAllById(roleIds)).thenReturn(List.of(otherRole));

        // when & then
        assertThatThrownBy(() ->
                companyService.assignRoleToWorker(ownerId, companyId1, workerCmId, roleIds)
        ).isInstanceOf(IllegalStateException.class);

        verify(companyMemberRoleRepository, never()).save(any());
    }
}