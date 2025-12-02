import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import SignScreen from '../app/screens/SignScreen';
import { signupApi } from '../app/services/auth';

jest.mock('../app/services/auth', () => ({
  signupApi: jest.fn(),
}));

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };

describe('SignScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Alert, 'alert');
  });

  it('화면 요소들이 올바르게 렌더링되어야 한다', () => {
    const { getByPlaceholderText, getAllByText } = render(
      <SignScreen navigation={mockNavigation} />
    );

    expect(getByPlaceholderText('이름')).toBeTruthy();
    expect(getByPlaceholderText('아이디 (이메일)')).toBeTruthy();
    expect(getByPlaceholderText('비밀번호')).toBeTruthy();
    
    expect(getAllByText('회원가입').length).toBe(2);
  });

  it('입력값이 비어있을 때 회원가입 버튼을 누르면 경고창이 떠야 한다', () => {
    const { getAllByText } = render( // getAllByText로 변경
      <SignScreen navigation={mockNavigation} />
    );


    const buttons = getAllByText('회원가입');
    fireEvent.press(buttons[1]);

    expect(signupApi).not.toHaveBeenCalled();
    expect(Alert.alert).toHaveBeenCalledWith("입력 오류", "이름, 아이디(이메일), 비밀번호를 모두 입력해주세요.");
  });

  it('모든 입력값이 유효할 때 API를 호출하고 성공 시 뒤로가기를 수행해야 한다', async () => {
    signupApi.mockResolvedValue({ message: "Success" });

    const { getByPlaceholderText, getAllByText } = render(
      <SignScreen navigation={mockNavigation} />
    );

    fireEvent.changeText(getByPlaceholderText('이름'), '테스트유저');
    fireEvent.changeText(getByPlaceholderText('아이디 (이메일)'), 'test@example.com');
    fireEvent.changeText(getByPlaceholderText('비밀번호'), 'password123');

    const buttons = getAllByText('회원가입');
    fireEvent.press(buttons[1]);

    await waitFor(() => {
      expect(signupApi).toHaveBeenCalledWith('test@example.com', 'password123', '테스트유저');
      expect(Alert.alert).toHaveBeenCalled(); 
    });
  });
});