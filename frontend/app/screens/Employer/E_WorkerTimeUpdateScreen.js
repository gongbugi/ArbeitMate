import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  FlatList,
} from "react-native";
import { ArrowLeft, ChevronDown } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function E_WorkerTimeUpdateScreen({ navigation, route }) {
  const { workerId, fixedTime } = route.params;

  
  const [dayOfWeek, setDayOfWeek] = useState(fixedTime?.day ?? "");
  const [startTime, setStartTime] = useState(fixedTime?.startTime ?? "");
  const [endTime, setEndTime] = useState(fixedTime?.endTime ?? "");
  const [dayModalVisible, setDayModalVisible] = useState(false);
  const [timeModalVisible, setTimeModalVisible] = useState(null); // "start" | "end"

  const days = ["월", "화", "수", "목", "금", "토", "일"];

  const timeOptions = Array.from({ length: 16 }, (_, i) => {
    const hour = 7 + i; // 7~22
    return `${String(hour).padStart(2, "0")}:00`;
  });

  /* ------- 저장 API ------- */
  const saveTime = async () => {
    if (!dayOfWeek || !startTime || !endTime) {
      alert("요일, 시작/종료 시간을 모두 선택하세요.");
      return;
    }

    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      await client.put(
        `/companies/${companyId}/workers/${workerId}/fixed-times/${fixedTime.id}`,
        {
          day: dayOfWeek,
          startTime,
          endTime,
        }
      );

      alert("근무 시간이 수정되었습니다.");
      navigation.goBack();
    } catch (err) {
      console.log("근무 시간 수정 오류:", err);
      alert("수정 실패");
    }
  };

  /* ------- 렌더링 ------- */

  const renderTimeOption = (time, target) => (
    <TouchableOpacity
      key={time}
      style={styles.optionItem}
      onPress={() => {
        target === "start" ? setStartTime(time) : setEndTime(time);
        setTimeModalVisible(null);
      }}
    >
      <Text style={styles.optionText}>{time}</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무 시간 수정</Text>
        <View style={{ width: 28 }} />
      </View>

      {/* 요일 선택 */}
      <Text style={styles.label}>요일</Text>
      <TouchableOpacity
        style={styles.selector}
        onPress={() => setDayModalVisible(true)}
      >
        <Text style={styles.selectorText}>{dayOfWeek || "요일 선택"}</Text>
        <ChevronDown size={20} color="#666" />
      </TouchableOpacity>

      {/* 시간 선택 */}
      <Text style={styles.label}>근무 시간</Text>

      <View style={styles.timeRow}>
        {/* 시작 */}
        <TouchableOpacity
          style={[styles.selector, { flex: 1 }]}
          onPress={() => setTimeModalVisible("start")}
        >
          <Text style={styles.selectorText}>
            {startTime || "시작 시간"}
          </Text>
          <ChevronDown size={20} color="#666" />
        </TouchableOpacity>

        <Text style={{ marginHorizontal: 8, fontSize: 16 }}>~</Text>

        {/* 종료 */}
        <TouchableOpacity
          style={[styles.selector, { flex: 1 }]}
          onPress={() => setTimeModalVisible("end")}
        >
          <Text style={styles.selectorText}>
            {endTime || "종료 시간"}
          </Text>
          <ChevronDown size={20} color="#666" />
        </TouchableOpacity>
      </View>

      {/* 저장 버튼 */}
      <TouchableOpacity style={styles.saveBtn} onPress={saveTime}>
        <Text style={styles.saveText}>저장</Text>
      </TouchableOpacity>

      {/* 요일 선택 모달 */}
      <Modal transparent visible={dayModalVisible} animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalBox}>
            {days.map((d) => (
              <TouchableOpacity
                key={d}
                style={styles.optionItem}
                onPress={() => {
                  setDayOfWeek(d);
                  setDayModalVisible(false);
                }}
              >
                <Text style={styles.optionText}>{d}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      </Modal>

      {/* 시간 선택 모달 */}
      <Modal transparent visible={!!timeModalVisible} animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalBox}>
            <FlatList
              data={timeOptions}
              keyExtractor={(item) => item}
              renderItem={({ item }) => renderTimeOption(item, timeModalVisible)}
            />
          </View>
        </View>
      </Modal>
    </View>
  );
}

/* ------- 스타일 ------- */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f8f8fb",
    paddingHorizontal: 20,
    paddingTop: 60,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 24,
    alignItems: "center",
  },
  headerTitle: { fontSize: 20, fontWeight: "bold" },

  label: {
    marginTop: 16,
    marginBottom: 6,
    color: "#555",
    fontSize: 14,
  },

  selector: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#f3f4f6",
    padding: 14,
    borderRadius: 12,
  },
  selectorText: {
    fontSize: 16,
    color: "#111",
  },

  timeRow: {
    flexDirection: "row",
    alignItems: "center",
  },

  saveBtn: {
    backgroundColor: "#000",
    paddingVertical: 14,
    borderRadius: 14,
    marginTop: 40,
  },
  saveText: {
    color: "#fff",
    textAlign: "center",
    fontWeight: "bold",
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.4)",
    justifyContent: "center",
    alignItems: "center",
  },
  modalBox: {
    backgroundColor: "#fff",
    width: "70%",
    maxHeight: "60%",
    borderRadius: 14,
    paddingVertical: 10,
  },

  optionItem: {
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  optionText: {
    fontSize: 16,
    color: "#111",
  },
});
