import axios from "axios";
import AsyncStorage from '@react-native-async-storage/async-storage';
import client from "./api";

const FIREBASE_API_KEY = "공유받은 Firebase API KEY값 입력";

export async function loginApi(email, password) {
  try {
    // 1. 구글(Firebase)에게 로그인 요청 -> 토큰 받기
    const firebaseRes = await axios.post(
      `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_API_KEY}`,
      { email: email,
        password: password,
        returnSecureToken: true,
      }
    );

    const idToken = firebaseRes.data.idToken;
    //받은 토큰을 폰에 저장 (api.js에서 사용)
    await AsyncStorage.setItem('userToken', idToken);
    console.log("Firebase 토큰 발급 성공:", idToken.substring(0, 10) + "...");

    // 2. 받은 토큰을 백엔드 서버에 전달 -> 최종 로그인
    const backendRes = await client.post("/auth/login", {
      idToken: idToken,
    });

    return backendRes.data; // 로그인 성공 정보 반환

  } catch (err) {
    console.error("로그인 실패:", err.response ? err.response.data : err);
    throw err;
  }
}
// 회원가입
export async function signupApi(email, password, name) {
  try {
    const response = await client.post("/auth/signup-email", {
      email,
      password,
      name,
    });
    return response.data;
  } catch (err) {
    console.error("회원가입 요청 실패:", err.response ? err.response.data : err);
    throw err;
  }
}