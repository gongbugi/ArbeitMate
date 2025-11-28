import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert, // 알림창 추가
} from "react-native";
import { loginApi } from "../services/auth"; // 작성한 api 함수 불러오기

export default function LoginScreen({ navigation }) {
  // 아이디(이메일), 비밀번호 상태 관리
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    // 1. 빈칸 검사
    if (!email || !password) {
      Alert.alert("입력 오류", "아이디와 비밀번호를 입력해주세요.");
      return;
    }

    try {
      // 2. 로그인 API 호출 (성공하면 토큰도 저장됨)
      await loginApi(email, password);

      // 3. 성공 시 다음 화면(근무지 선택)으로 이동
      // refresh: true를 보내서 근무지 목록을 새로고침하게 함
      navigation.navigate("WorkplaceSelectScreen", { refresh: true });

    } catch (err) {
      // 4. 실패 시 에러 처리
      Alert.alert("로그인 실패", "이메일 또는 비밀번호를 확인해주세요.");
    }
  };
  
  return (
    <View style={styles.container}>

      {/* 로고 텍스트 */}
      <Text style={styles.logo}>ArbeitMate</Text>

      {/* 아이디 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="아이디 (이메일)"
          placeholderTextColor="#9CA3AF"
          style={styles.input}
          keyboardType="email-address"
          autoCapitalize="none"
          value={email}
          onChangeText={setEmail}
        />
      </View>

      {/* 비밀번호 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="비밀번호"
          placeholderTextColor="#9CA3AF"
          secureTextEntry
          style={styles.input}
          value={password}
          onChangeText={setPassword}
        />
      </View>

      {/* 로그인 버튼 */}
      <TouchableOpacity style={styles.loginButton} onPress={() => navigation.navigate("WorkplaceSelectScreen")}>
        <Text style={styles.loginText}>로그인</Text>
      </TouchableOpacity>

      {/* 회원가입 버튼 (가입 화면으로 이동) */}
      <TouchableOpacity 
        style={styles.signupContainer}
        onPress={() => navigation.navigate("SignScreen")} // 회원가입 화면 연결
      >
        <Text style={styles.signupText}>회원가입</Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6", // gray-100
    paddingHorizontal: 24,      // px-6
    paddingTop: 110,            // pt-28
  },

  logo: {
    fontSize: 36, // text-4xl
    fontWeight: "bold",
    color: "#000",
    textAlign: "center",
    marginBottom: 80, // mb-20
  },

  inputBox: {
    backgroundColor: "#fff",
    borderRadius: 16, // rounded-2xl
    height: 64,       // h-16
    justifyContent: "center",
    paddingHorizontal: 16, // px-4
    marginBottom: 24,      // mb-6
  },

  input: {
    fontSize: 16,   // text-base
    color: "#000",
  },

  loginButton: {
    backgroundColor: "#000",
    borderRadius: 24, // rounded-3xl
    height: 56,       // h-14
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 24, // mb-6
  },

  loginText: {
    fontSize: 24,     // text-2xl
    fontWeight: "bold",
    color: "#fff",
  },

  signupContainer: {
    alignItems: "flex-end",
    marginTop: 8,
  },

  signupText: {
    fontSize: 20,      // text-xl
    color: "#6b7280",  // gray-500
  },
});