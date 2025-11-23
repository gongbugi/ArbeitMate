import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  TextInput,
  StyleSheet,
} from "react-native";
import { ArrowLeft, Plus, Minus } from "lucide-react-native";

export default function E_ScheduleAutoAddScreen({ navigation }) {
  const [count, setCount] = useState(0);

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

      {/* 요일 */}
      <Text style={styles.dayTitle}>{day}</Text>

      {/* 팝업 배경 */}
      <View style={styles.dimmed} />

      {/* 팝업 박스 */}
      <View style={styles.modalBox}>

        <Text style={styles.modalTitle}>인원 추가</Text>

        {/* 담당 업무 */}
        <Text style={styles.label}>담당 업무</Text>
        <TextInput
          placeholder="입력"
          style={styles.input}
        />

        {/* 업무 시간 */}
        <Text style={styles.label}>업무 시간</Text>
        <TextInput
          placeholder="예: 12:00 - 14:00"
          style={styles.input}
        />

        {/* 필요 인원 */}
        <View style={styles.rowBetween}>
          <Text style={styles.label}>필요 인원</Text>

          <View style={styles.counterRow}>
            <TouchableOpacity
              style={styles.counterBtn}
              onPress={() => setCount(Math.max(0, count - 1))}
            >
              <Minus size={20} color="#000" />
            </TouchableOpacity>

            <Text style={styles.countText}>{count}</Text>

            <TouchableOpacity
              style={styles.counterBtn}
              onPress={() => setCount(count + 1)}
            >
              <Plus size={20} color="#000" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 확인 버튼 */}
        <TouchableOpacity style={styles.saveBtn}>
          <Text style={styles.saveText}>확인</Text>
        </TouchableOpacity>

      </View>

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

  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },

  dayTitle: {
    fontSize: 20,
    fontWeight: "bold",
    marginTop: 20,
    color: "#000",
  },

  dimmed: {
    position: "absolute",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    backgroundColor: "rgba(0,0,0,0.5)",
  },

  modalBox: {
    position: "absolute",
    left: 30,
    right: 30,
    top: 200,
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 30,
    paddingHorizontal: 24,
  },

  modalTitle: {
    fontSize: 26,
    fontWeight: "600",
    textAlign: "center",
    marginBottom: 20,
    color: "#000",
  },

  label: {
    fontSize: 16,
    fontWeight: "600",
    marginBottom: 8,
    marginTop: 16,
    color: "#000",
  },

  input: {
    backgroundColor: "#f1f1f1",
    borderRadius: 20,
    paddingHorizontal: 15,
    paddingVertical: 10,
    fontSize: 15,
  },

  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginTop: 16,
  },

  counterRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 15,
  },
  counterBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: "#eee",
    justifyContent: "center",
    alignItems: "center",
  },
  countText: {
    fontSize: 22,
    fontWeight: "600",
  },

  saveBtn: {
    backgroundColor: "#000",
    borderRadius: 20,
    paddingVertical: 14,
    alignItems: "center",
    marginTop: 30,
  },
  saveText: {
    fontSize: 18,
    fontWeight: "700",
    color: "#fff",
  },
});
