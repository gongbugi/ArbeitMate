import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import { signupApi } from "../services/auth";

export default function SignScreen({ navigation }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");


  // 회원가입 버튼 눌렀을 때 실행될 함수
  const handleSignup = async () => {
    // 1. 빈칸 검사
    if (!name || !email || !password) {
      Alert.alert("입력 오류", "이름, 아이디(이메일), 비밀번호를 모두 입력해주세요.");
      return;
    }

    try {
      // 2. API 호출
      await signupApi(email, password, name);
      
      // 3. 성공 시 알림 & 로그인 화면으로 이동
      Alert.alert("가입 성공", "회원가입이 완료되었습니다! 로그인해주세요.", [
        { text: "확인", onPress: () => navigation.goBack() } // 확인 누르면 뒤로가기
      ]);

    } catch (err) {
      // 4. 실패 시 에러 처리
      const message = err.response?.data?.message || "회원가입에 실패했습니다.";
      Alert.alert("가입 실패", message);
    }
  };

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>회원가입</Text>
      </View>

      {/* Logo */}
      <Text style={styles.logoText}>ArbeitMate</Text>

      {/* 이름 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="이름"
          placeholderTextColor="#9CA3AF"
          style={styles.inputText}
          value={name}           // 상태 연결
          onChangeText={setName} // 입력 시 상태 업데이트
        />
      </View>

      {/* 아이디 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="아이디 (이메일)"
          placeholderTextColor="#9CA3AF"
          style={styles.inputText}
          keyboardType="email-address" // 이메일용 키보드
          autoCapitalize="none"        // 첫 글자 대문자 방지
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
          style={styles.inputText}
          value={password}
          onChangeText={setPassword}
        />
      </View>

      {/* 회원가입 버튼 */}
      <TouchableOpacity style={styles.button} onPress={handleSignup}>
        <Text style={styles.buttonText}>회원가입</Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6", // gray-100
    paddingHorizontal: 24,
    paddingTop: 64,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 64,
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  logoText: {
    fontSize: 36,
    fontWeight: "bold",
    textAlign: "center",
    color: "#000",
    marginBottom: 64,
  },

  inputBox: {
    backgroundColor: "#fff",
    borderRadius: 16,
    height: 64,
    justifyContent: "center",
    paddingHorizontal: 16,
    marginBottom: 24,
  },

  inputText: {
    fontSize: 16,
    color: "#000",
  },

  button: {
    height: 56,
    backgroundColor: "#000",
    borderRadius: 30,
    justifyContent: "center",
    alignItems: "center",
    marginTop: 8,
  },

  buttonText: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#fff",
  },
});
