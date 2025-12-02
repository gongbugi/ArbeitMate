import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import ScheduleScreen from '../app/screens/ScheduleScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

jest.mock('@expo/vector-icons', () => ({ Ionicons: 'Ionicons' }), { virtual: true });
jest.mock('lucide-react-native', () => ({ ArrowLeft: 'ArrowLeft' }));

jest.mock('react-native-calendars', () => ({
  Calendar: () => 'CalendarComponent',
  LocaleConfig: { locales: {} }
}));

jest.mock('../app/services/api');
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
}));

jest.mock('@react-navigation/native', () => {
  const actualNav = jest.requireActual('@react-navigation/native');
  const React = require('react'); // Mock 내부에서 React require
  return {
    ...actualNav,
    useFocusEffect: (callback) => React.useEffect(callback, []),
  };
});

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };

describe('ScheduleScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    AsyncStorage.getItem.mockResolvedValue('test-company-id');
  });

  it('고용주(OWNER)일 경우 전체 근무표 조회 API를 호출해야 한다', async () => {
    client.get.mockImplementation((url) => {
      if (url === '/companies/me') {
        return Promise.resolve({ data: [{ companyId: 'test-company-id', role: 'OWNER' }] });
      }
      if (url.includes('/schedule/monthly')) {
        return Promise.resolve({ data: [] });
      }
      return Promise.resolve({ data: {} });
    });

    const { getByText } = render(<ScheduleScreen navigation={mockNavigation} />);

    await waitFor(() => {
      expect(getByText('전체 근무표')).toBeTruthy();
      
      expect(client.get).toHaveBeenCalledWith(
        expect.stringContaining('/schedule/monthly'),
        expect.anything()
      );
    });
  });

  it('근무자(WORKER)일 경우 내 급여/근무 조회 API를 호출해야 한다', async () => {
    client.get.mockImplementation((url) => {
      if (url === '/companies/me') {
        return Promise.resolve({ data: [{ companyId: 'test-company-id', role: 'WORKER' }] });
      }
      if (url.includes('/salary')) {
        return Promise.resolve({ data: { details: [] } });
      }
      return Promise.resolve({ data: {} });
    });

    const { getByText } = render(<ScheduleScreen navigation={mockNavigation} />);

    await waitFor(() => {
      expect(getByText('내 근무표')).toBeTruthy();

      expect(client.get).toHaveBeenCalledWith(
        expect.stringContaining('/salary'),
        expect.anything()
      );
    });
  });
});