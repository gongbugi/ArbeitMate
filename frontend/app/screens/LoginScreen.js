import React from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { loginApi } from "../services/auth";

export default function LoginScreen({ navigation, onLogin }) {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");

  const handleLoginPress = async () => {
    try {
      const userData = await loginApi(loginId, password);

      // userData 안에 token, userId, role 이 들어있음
      onLogin(userData);

    } catch (err) {
      Alert.alert("로그인 실패", "아이디 또는 비밀번호가 잘못됐습니다.");
    }
  };
  
  return (
    <View style={styles.container}>

      {/* 로고 텍스트 */}
      <Text style={styles.logo}>ArbeitMate</Text>

      {/* 아이디 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="아이디"
          placeholderTextColor="#9CA3AF"
          style={styles.input}
        />
      </View>

      {/* 비밀번호 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="비밀번호"
          placeholderTextColor="#9CA3AF"
          secureTextEntry
          style={styles.input}
        />
      </View>

      {/* 로그인 버튼 */}
      <TouchableOpacity style={styles.loginButton}
      onPress={() => navigation.navigate("WorkplaceSelectScreen")}> //테스트용
        <Text style={styles.loginText}>로그인</Text>
      </TouchableOpacity>

      {/* 회원가입 */}
      <TouchableOpacity style={styles.signupContainer}>
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
    marginBottom: 24,      // mb-6 (비번은 아래서 조정됨)
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
