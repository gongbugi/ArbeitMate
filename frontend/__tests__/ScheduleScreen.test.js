import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import ScheduleScreen from '../app/screens/ScheduleScreen';
import client from '../app/services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

// 1. 필요한 모듈 Mocking
jest.mock('@expo/vector-icons', () => ({ Ionicons: 'Ionicons' }), { virtual: true });
jest.mock('lucide-react-native', () => ({ ArrowLeft: 'ArrowLeft' }));

// 2. react-native-calendars Mocking
jest.mock('react-native-calendars', () => ({
  Calendar: () => 'CalendarComponent',
  LocaleConfig: { locales: {} }
}));

jest.mock('../app/services/api');
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
}));

// [수정 포인트] useFocusEffect를 useEffect로 대체하여 렌더링 이후에 실행되도록 변경
jest.mock('@react-navigation/native', () => {
  const actualNav = jest.requireActual('@react-navigation/native');
  const React = require('react'); // Mock 내부에서 React require
  return {
    ...actualNav,
    useFocusEffect: (callback) => React.useEffect(callback, []), // useEffect로 감싸기
  };
});

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };

describe('ScheduleScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    AsyncStorage.getItem.mockResolvedValue('test-company-id');
  });

  it('고용주(OWNER)일 경우 전체 근무표 조회 API를 호출해야 한다', async () => {
    // Role: OWNER 설정
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
      // 헤더 제목 확인
      expect(getByText('전체 근무표')).toBeTruthy();
      
      // 고용주용 API 호출 확인
      expect(client.get).toHaveBeenCalledWith(
        expect.stringContaining('/schedule/monthly'),
        expect.anything()
      );
    });
  });

  it('근무자(WORKER)일 경우 내 급여/근무 조회 API를 호출해야 한다', async () => {
    // Role: WORKER 설정
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
      // 헤더 제목 확인
      expect(getByText('내 근무표')).toBeTruthy();

      // 근무자용 API 호출 확인 (급여 API 활용)
      expect(client.get).toHaveBeenCalledWith(
        expect.stringContaining('/salary'),
        expect.anything()
      );
    });
  });
});