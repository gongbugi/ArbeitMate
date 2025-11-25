import React, { useState } from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import E_ScheduleAutoAddPeriodSelectScreen from "./E_ScheduleAutoAddPeriodSelectScreen";
import axios from "axios";

export default function E_ScheduleAutoAddPeriodScreen({ navigation }) {
  const [calendarVisible, setCalendarVisible] = useState(false);
  const [selectedDate, setSelectedDate] = useState("");
  const BASE_URL = "http://<백엔드-서버-ip>:8080"; 

  async function savePeriod() {
  try {
    const res = await axios.post(`.../api/schedules/period`, {
      startDate: selectedStart,
      endDate: selectedEnd,
      defaultPeople: needPeople
    });

    console.log("저장 성공", res.data);

    navigation.navigate("E_ScheduleAutoAddWeekdayScreen", {
      period: `${selectedStart} ~ ${selectedEnd}`
    });

  } catch (error) {
    console.error("저장 실패", error);
  }
}

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

       <Text style={styles.label}>기간</Text>

    <TouchableOpacity onPress={() => setCalendarVisible(true)}>
      <View style={styles.inputBox}>
      <TextInput
        placeholderTextColor="#999"
        style={styles.input}
        value={selectedDate}
        editable={false}
        pointerEvents="none"   // TextInput 클릭 막기
        />
      </View>
    </TouchableOpacity>

      {/* 필요 인원 */}
      <Text style={styles.label}>필요 인원</Text>
      <View style={styles.inputBox}>
        <TextInput
          placeholderTextColor="#999"
          keyboardType="numeric"
          editable={false}
          pointerEvents="none"
        />
      </View>

      {/* 저장 버튼 */}
      <TouchableOpacity
          style={styles.saveBtn}
          onPress={() => {
          navigation.navigate("E_ScheduleAutoAddWeekdayScreen", {
          period: selectedDate,  // 선택한 기간을 전달
         });
        }}
      >
        <Text style={styles.saveText}>저장</Text>
      </TouchableOpacity>

       <E_ScheduleAutoAddPeriodSelectScreen
        visible={calendarVisible}
        onClose={() => setCalendarVisible(false)}
        onSelect={(date) => {
          setSelectedDate(date);
          setCalendarVisible(false);
        }}
      />

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
