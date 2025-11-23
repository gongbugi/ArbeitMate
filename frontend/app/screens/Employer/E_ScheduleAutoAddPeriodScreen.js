import React from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ScheduleAutoAddPeriodScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무표 자동 생성</Text>

        <View style={{ width: 32 }} />
      </View>

      {/* 기간 */}
      <Text style={styles.label}
      onPress={() => navigation.navigate("E_ScheduleAutoAddPeriodSelectScreen")}>기간</Text>
      <View style={styles.inputBox}>
        <TextInput
          placeholder="예: 10/01 ~ 10/15"
          placeholderTextColor="#999"
          style={styles.input}
        />
      </View>

      {/* 필요 인원 */}
      <Text style={styles.label}>필요 인원</Text>
      <View style={styles.inputBox}>
        <TextInput
          placeholder="예: 2명"
          placeholderTextColor="#999"
          style={styles.input}
          keyboardType="numeric"
        />
      </View>

      {/* 저장 버튼 */}
      <TouchableOpacity style={styles.saveBtn}>
        <Text style={styles.saveText}>저장</Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F3F4F6", // 배경
    paddingTop: 80,
    paddingHorizontal: 24,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 40,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },

  label: {
    fontSize: 18,
    fontWeight: "700",
    color: "#000",
    marginBottom: 10,
    marginLeft: 4,
  },

  inputBox: {
    backgroundColor: "#fff",
    height: 52,
    borderRadius: 30,
    paddingHorizontal: 20,
    justifyContent: "center",
    marginBottom: 40,
  },
  input: {
    fontSize: 16,
    color: "#000",
  },

  saveBtn: {
    backgroundColor: "#000",
    borderRadius: 30,
    height: 56,
    justifyContent: "center",
    alignItems: "center",
    marginTop: 80,
  },
  saveText: {
    color: "#fff",
    fontSize: 20,
    fontWeight: "bold",
  },
});
