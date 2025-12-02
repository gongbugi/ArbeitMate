import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import W_HomeScreen from '../app/screens/Worker/W_HomeScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

jest.mock('lucide-react-native', () => ({
  Bell: () => 'BellIcon',
  UserCircle: () => 'UserCircleIcon',
  ChevronRight: () => 'ChevronRightIcon'
}));

jest.mock('../app/services/api');
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
}));

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useFocusEffect: (callback) => callback(),
}));

const mockNavigation = { navigate: jest.fn() };

describe('W_HomeScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('화면 로드 시 저장된 근무지 이름과 API로 가져온 급여 정보를 표시해야 한다', async () => {
    AsyncStorage.getItem.mockImplementation((key) => {
      if (key === 'currentCompanyId') return Promise.resolve('101');
      if (key === 'currentCompanyName') return Promise.resolve('스타벅스 역삼점');
      return Promise.resolve(null);
    });

    client.get.mockResolvedValue({
      data: { totalSalary: 1500000 }
    });

    const { getByText, findByText } = render(
      <W_HomeScreen navigation={mockNavigation} />
    );

    await findByText('스타벅스 역삼점');

    await waitFor(() => {
      const now = new Date();
      expect(client.get).toHaveBeenCalledWith(
        '/companies/101/salary',
        expect.objectContaining({
          params: expect.objectContaining({
            year: now.getFullYear(),
            month: now.getMonth() + 1
          })
        })
      );
    });

    await findByText('1,500,000 원');
  });
});