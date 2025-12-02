import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import E_ScheduleAutoAddSummaryScreen from '../app/screens/Employer/E_ScheduleAutoAddSummaryScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

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
    
    global.alert = jest.fn(); 
    
    jest.spyOn(Alert, 'alert'); 
    
    AsyncStorage.getItem.mockResolvedValue('company-123');
  });

  it('설정된 패턴에 따라 필요 인원 합계를 계산하고, 생성 버튼 클릭 시 API를 호출해야 한다', async () => {
    client.get.mockResolvedValue({
      data: [
        { submitted: true }, { submitted: true }, { submitted: true },
        { submitted: true }, { submitted: true }
      ]
    });

    client.post.mockResolvedValue({ data: { message: "success" } });

    const { getByText } = render(
      <E_ScheduleAutoAddSummaryScreen navigation={mockNavigation} route={mockRoute} />
    );

    expect(getByText('필요 인원')).toBeTruthy();
    
    await waitFor(() => {
      expect(getByText('5명')).toBeTruthy(); 
    });

    const createBtn = getByText('생성');
    fireEvent.press(createBtn);

    await waitFor(() => {
      expect(client.post).toHaveBeenCalledWith(
        '/companies/company-123/schedule/period-123/create/slots',
        expect.objectContaining({ slots: expect.any(Array) })
      );

      expect(client.post).toHaveBeenCalledWith(
        '/companies/company-123/schedule/period-123/auto-assign'
      );

      expect(global.alert).toHaveBeenCalled();
      
      expect(mockNavigation.goBack).toHaveBeenCalled();
    });
  }, 15000);
});