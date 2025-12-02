import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import WorkplaceAddScreen from '../app/screens/WorkplaceAddScreen';
import client from '../app/services/api';


jest.mock('@expo/vector-icons', () => ({
  Ionicons: 'Ionicons',
  ArrowLeft: 'ArrowLeft'
}), { virtual: true });

jest.mock('lucide-react-native', () => ({
  ArrowLeft: () => 'ArrowLeftIcon'
}));

jest.mock('@react-native-async-storage/async-storage', () => ({
    setItem: jest.fn(),
    getItem: jest.fn(),
    removeItem: jest.fn(),
    clear: jest.fn(),
  }));

jest.mock('../app/services/api');

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };

describe('WorkplaceAddScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert');
  });

  it('매장명과 주소 입력 필드가 렌더링되어야 한다', () => {
    const { getByPlaceholderText, getByText } = render(
      <WorkplaceAddScreen navigation={mockNavigation} />
    );

    expect(getByPlaceholderText('매장명 입력')).toBeTruthy();
    expect(getByPlaceholderText('주소 입력')).toBeTruthy();
    expect(getByText('등록')).toBeTruthy();
  });

  it('입력값이 없을 때 등록 버튼을 누르면 경고창이 떠야 한다', () => {
    const { getByText } = render(
      <WorkplaceAddScreen navigation={mockNavigation} />
    );

    fireEvent.press(getByText('등록'));

    expect(client.post).not.toHaveBeenCalled();
    expect(Alert.alert).toHaveBeenCalledWith("입력 오류", "매장명과 주소는 필수 입력 사항입니다.");
  });

  it('유효한 입력값으로 등록 시 API를 호출하고 성공하면 목록 화면으로 이동해야 한다', async () => {
    // API 성공 응답 설정
    client.post.mockResolvedValue({ data: { message: "Success" } });

    const { getByPlaceholderText, getByText } = render(
      <WorkplaceAddScreen navigation={mockNavigation} />
    );

    fireEvent.changeText(getByPlaceholderText('매장명 입력'), '테스트 카페');
    fireEvent.changeText(getByPlaceholderText('주소 입력'), '서울시 강남구');

    fireEvent.press(getByText('등록'));

    await waitFor(() => {
      expect(client.post).toHaveBeenCalledWith("/companies/create", {
        companyName: '테스트 카페',
        companyAddress: '서울시 강남구',
      });
      
      expect(Alert.alert).toHaveBeenCalled();  

    });
  });
});