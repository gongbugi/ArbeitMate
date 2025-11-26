import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function E_ScheduleAutoAddWeekdayScreen({ navigation, route }) {

  const days = ["월", "화", "수", "목", "금", "토", "일"];
const [selectedDate, setSelectedDate] = useState("");
  const period = route.params?.period || "기간을 선택하세요";

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

      <ScrollView showsVerticalScrollIndicator={false}>

        {/* 기간 */}
        <Text style={styles.sectionTitle}>기간</Text>
        <View style={styles.box}>
          <Text style={styles.boxText}>{period}</Text>
        </View>

        {/* 필요 인원 */}
        <Text style={styles.sectionTitle}>필요 인원</Text>


        {/* 요일 리스트 */}
        <View style={styles.dayList}>
          {days.map((day, idx) => (
            <TouchableOpacity key={idx} style={styles.dayRow}
              onPress={() => navigation.navigate("E_ScheduleAutoAddPeopleScreen", {
                day: day,
                period: period,
              })}>
              <Text style={styles.dayText}>{day}</Text>
              <ChevronRight size={28} color="#999" />
            </TouchableOpacity>
          ))}
        </View>

      </ScrollView>

      {/* 저장 버튼 */}
      <TouchableOpacity style={styles.saveBtn}
        onPress={() => {
          navigation.navigate("E_ScheduleAutoAddSummaryScreen", {
            period: period,  // 선택한 기간을 전달
          });
        }}>
        <Text style={styles.saveText}>저장</Text>
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

  sectionTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
    marginTop: 20,
    marginBottom: 8,
  },

  box: {
    width: "100%",
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 14,
    paddingHorizontal: 20,
    elevation: 2,
  },
  boxText: {
    fontSize: 16,
    color: "#000",
  },

  dayList: {
    marginTop: 30,
  },

  dayRow: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    marginBottom: 14,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 2,
  },

  dayText: {
    fontSize: 22,
    fontWeight: "500",
    color: "#000",
  },

  saveBtn: {
    backgroundColor: "#000",
    height: 60,
    borderRadius: 30,
    alignItems: "center",
    justifyContent: "center",
    marginVertical: 20,
  },
  saveText: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#fff",
  },
});
