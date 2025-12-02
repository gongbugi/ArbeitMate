import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import WorkplaceSelectScreen from '../app/screens/WorkplaceSelectScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

jest.mock('@expo/vector-icons', () => ({
    Ionicons: 'Ionicons',
  }), { virtual: true });

jest.mock('../app/services/api');

jest.mock('@react-native-async-storage/async-storage', () => ({
  setItem: jest.fn(),
  getItem: jest.fn(),
}));

const mockNavigation = { navigate: jest.fn() };
const mockSetRole = jest.fn();

describe('WorkplaceSelectScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('API로부터 근무지 목록을 가져와 렌더링해야 한다', async () => {
    const mockWorkplaces = [
      { companyId: '101', companyName: '스타벅스 강남점', role: 'OWNER' },
      { companyId: '102', companyName: '맥도날드 홍대점', role: 'WORKER' }
    ];
    
    client.get.mockResolvedValue({ data: mockWorkplaces });

    const { getByText, findByText } = render(
      <WorkplaceSelectScreen 
        navigation={mockNavigation} 
        route={{ params: {} }} 
        setRole={mockSetRole} 
      />
    );

    expect(getByText('근무지')).toBeTruthy();


    await findByText('스타벅스 강남점');
    await findByText('맥도날드 홍대점');
    
    expect(getByText('고용주')).toBeTruthy();
    expect(getByText('근무자')).toBeTruthy();
  });

  it('근무지를 선택하면 AsyncStorage에 저장하고 역할을 설정해야 한다', async () => {
    const mockWorkplaces = [
      { companyId: '101', companyName: '테스트 매장', role: 'OWNER' }
    ];
    client.get.mockResolvedValue({ data: mockWorkplaces });

    const { findByText } = render(
      <WorkplaceSelectScreen 
        navigation={mockNavigation} 
        route={{ params: {} }} 
        setRole={mockSetRole} 
      />
    );

    const workplaceItem = await findByText('테스트 매장');
    fireEvent.press(workplaceItem);

    await waitFor(() => {
      expect(AsyncStorage.setItem).toHaveBeenCalledWith("currentCompanyId", "101");
      expect(AsyncStorage.setItem).toHaveBeenCalledWith("currentCompanyName", "테스트 매장");
      
      expect(mockSetRole).toHaveBeenCalledWith("employer");
    });
  });

  it('신규 등록 버튼을 누르면 WorkplaceRegisterScreen으로 이동해야 한다', async () => {
    client.get.mockResolvedValue({ data: [] });

    const { findByText } = render(
        <WorkplaceSelectScreen 
          navigation={mockNavigation} 
          route={{ params: {} }} 
          setRole={mockSetRole} 
        />
      );

    const addButton = await findByText('신규등록');
    fireEvent.press(addButton);

    expect(mockNavigation.navigate).toHaveBeenCalledWith('WorkplaceRegisterScreen');
  });
});