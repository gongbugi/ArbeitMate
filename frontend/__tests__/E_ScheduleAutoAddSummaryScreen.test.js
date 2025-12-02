import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import E_ScheduleAutoAddSummaryScreen from '../app/screens/Employer/E_ScheduleAutoAddSummaryScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

// 1. 아이콘 및 네비게이션 Mocking
jest.mock('lucide-react-native', () => ({
  ArrowLeft: 'ArrowLeft'
}));

jest.mock('../app/services/api');
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
}));

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };

const mockRoute = {
  params: {
    periodId: 'period-123',
    startDate: '2025-11-01',
    endDate: '2025-11-07', 
    periodLabel: '2025-11-01 ~ 2025-11-07',
    weekdayConfigs: {
      0: [{ roleId: 'role-1', requiredHeadCount: 2 }], 
      1: [{ roleId: 'role-1', requiredHeadCount: 1 }], 
    }
  }
};

describe('E_ScheduleAutoAddSummaryScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // [수정] React Native의 Alert.alert가 아니라 전역 alert()를 사용하므로 이를 모킹해야 함
    global.alert = jest.fn(); 
    
    // 혹시 모를 Alert.alert 사용 대비
    jest.spyOn(Alert, 'alert'); 
    
    AsyncStorage.getItem.mockResolvedValue('company-123');
  });

  it('설정된 패턴에 따라 필요 인원 합계를 계산하고, 생성 버튼 클릭 시 API를 호출해야 한다', async () => {
    // 1. 응답 인원 조회 API Mock
    client.get.mockResolvedValue({
      data: [
        { submitted: true }, { submitted: true }, { submitted: true },
        { submitted: true }, { submitted: true }
      ]
    });

    // 2. 생성 API 성공 응답 Mock
    client.post.mockResolvedValue({ data: { message: "success" } });

    const { getByText } = render(
      <E_ScheduleAutoAddSummaryScreen navigation={mockNavigation} route={mockRoute} />
    );

    // 3. 화면 렌더링 및 계산 검증
    expect(getByText('필요 인원')).toBeTruthy();
    
    await waitFor(() => {
      expect(getByText('5명')).toBeTruthy(); 
    });

    // 4. 생성 버튼 클릭
    const createBtn = getByText('생성');
    fireEvent.press(createBtn);

    // 5. API 호출 순서 검증
    await waitFor(() => {
      // (1) 슬롯 생성 API 호출
      expect(client.post).toHaveBeenCalledWith(
        '/companies/company-123/schedule/period-123/create/slots',
        expect.objectContaining({ slots: expect.any(Array) })
      );

      // (2) 자동 배정 API 호출
      expect(client.post).toHaveBeenCalledWith(
        '/companies/company-123/schedule/period-123/auto-assign'
      );

      // (3) alert 호출 확인 (global.alert 호출 확인)
      expect(global.alert).toHaveBeenCalled();
      
      // (4) 뒤로가기 이동
      expect(mockNavigation.goBack).toHaveBeenCalled();
    });
  });
});