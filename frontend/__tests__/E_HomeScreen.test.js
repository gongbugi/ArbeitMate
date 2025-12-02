import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import E_HomeScreen from '../app/screens/Employer/E_HomeScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

// 아이콘 Mocking
jest.mock('lucide-react-native', () => ({
  Bell: 'Bell',
  Home: 'Home',
  ChevronRight: 'ChevronRight'
}));

jest.mock('../app/services/api');
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
}));

const mockNavigation = { navigate: jest.fn() };

describe('E_HomeScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    AsyncStorage.getItem.mockResolvedValue('test-company-id');
  });

  it('화면 로드 시 미지급 급여 정보를 가져와서 표시해야 한다', async () => {
    // API 응답 Mock (미지급 급여 2,000,000원)
    client.get.mockResolvedValue({
      data: { unpaidSalaryTotal: 2000000 }
    });

    const { getByText } = render(<E_HomeScreen navigation={mockNavigation} />);

    await waitFor(() => {
      // API 호출 검증
      expect(client.get).toHaveBeenCalledWith(
        '/companies/test-company-id/salary',
        expect.anything()
      );

      // 금액 포맷팅 및 표시 검증
      expect(getByText('지급해야 할 돈')).toBeTruthy();
      expect(getByText('- 2,000,000 원')).toBeTruthy();
    });
  });
});