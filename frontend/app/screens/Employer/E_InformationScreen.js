import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_InformationScreen({ navigation }) {
  return (
    <View style={styles.container}>
      
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무지 등록</Text>
      </View>

      {/* 입력 필드 */}
      <View style={styles.inputContainer}>
        <Text style={styles.label}>매장명</Text>
        <View style={styles.inputBox}>
          <Text style={styles.inputText}>근무지 1</Text>
        </View>

        <Text style={styles.label}>주소</Text>
        <View style={styles.inputBox}>
          <Text style={styles.inputText}>경기도 수원시 영통구</Text>
        </View>

        <Text style={styles.label}>업종</Text>
        <View style={styles.inputBox}>
          <Text style={styles.inputText}>외식업</Text>
        </View>

        <Text style={styles.label}>전화번호</Text>
        <View style={styles.inputBox}>
          <Text style={styles.inputText}>031 - xxx - xxxx</Text>
        </View>

        <Text style={styles.label}>초대코드</Text>
        <View style={styles.inputBox}>
          <Text style={styles.inputText}>AWDVZ</Text>
        </View>
      </View>

      {/* 등록 버튼 */}
      <TouchableOpacity style={styles.button}>
        <Text style={styles.buttonText}>등록</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",
    paddingHorizontal: 24,
    paddingTop: 64,
  },

  /* Header */
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    marginLeft: 16,
    color: "#000",
  },

  /* Label + Input */
  inputContainer: {
    marginBottom: 40,
  },
  label: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
    marginLeft: 6,
  },
  inputBox: {
    backgroundColor: "#fff",
    height: 50,
    borderRadius: 30,
    justifyContent: "center",
    paddingHorizontal: 20,
    marginBottom: 24,
  },
  inputText: {
    fontSize: 18,
    color: "#000",
  },

  /* Button */
  button: {
    backgroundColor: "#000",
    paddingVertical: 16,
    borderRadius: 30,
    marginTop: 40,
  },
  buttonText: {
    textAlign: "center",
    color: "#fff",
    fontSize: 22,
    fontWeight: "bold",
  },
});
