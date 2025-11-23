import React from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function SignScreen({ navigation }) {
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
        />
      </View>

      {/* 아이디 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="아이디"
          placeholderTextColor="#9CA3AF"
          style={styles.inputText}
        />
      </View>

      {/* 비밀번호 입력 */}
      <View style={styles.inputBox}>
        <TextInput
          placeholder="비밀번호"
          placeholderTextColor="#9CA3AF"
          secureTextEntry
          style={styles.inputText}
        />
      </View>

      {/* 회원가입 버튼 */}
      <TouchableOpacity style={styles.button}>
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
