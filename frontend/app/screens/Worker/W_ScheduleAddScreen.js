import React from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function W_ScheduleAddScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무 시간 등록</Text>
      </View>

      {/* 시간 */}
      <Text style={styles.label}>시간</Text>
      <TextInput
        placeholder="예: 12:00 - 16:00"
        placeholderTextColor="#9CA3AF"
        style={styles.input}
      />

      {/* 요일 */}
      <Text style={[styles.label, { marginTop: 24 }]}>요일</Text>
      <TextInput
        placeholder="예: 월, 화, 금"
        placeholderTextColor="#9CA3AF"
        style={styles.input}
      />

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
    backgroundColor: "#f4f4f5", // bg-gray-100
    paddingHorizontal: 24,       // px-6
    paddingTop: 64,              // pt-16
  },

  /** HEADER */
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 24,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /** LABEL */
  label: {
    fontSize: 20,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
  },

  /** INPUT */
  input: {
    width: "100%",
    height: 48,
    backgroundColor: "#fff",
    borderRadius: 16,
    paddingHorizontal: 16,
    fontSize: 18,
    color: "#000",
  },

  /** BUTTON */
  button: {
    width: "100%",
    height: 56,
    backgroundColor: "#000",
    borderRadius: 24,
    justifyContent: "center",
    alignItems: "center",
    marginTop: 48,
  },
  buttonText: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#fff",
  },
});
