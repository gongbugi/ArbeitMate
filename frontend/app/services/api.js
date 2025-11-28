import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// 안드로이드 에뮬레이터 기준 주소 (실제 폰 사용 시 내 PC IP로 변경 필요)
const BASE_URL = "http://162.120.185.40:8080"; 

const client = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// 요청 보낼 때마다 토큰이 있으면 자동으로 붙여주는 설정
client.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('userToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default client;