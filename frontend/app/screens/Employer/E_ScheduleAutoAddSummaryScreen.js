import React from "react";
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ScheduleAutoAddSummaryScreen({ navigation }) {
  const days = ["월", "화", "수", "목", "금", "토", "일"];

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무표 자동 생성</Text>
      </View>

      {/* 기간 */}
      <Text style={styles.period}>10.18 (토) - 10.24 (금)</Text>

      {/* 카드 */}
      <View style={styles.card}>

        <ScrollView showsVerticalScrollIndicator={false}>
          {days.map((day, idx) => (
            <TouchableOpacity
              key={idx}
              style={styles.dayRow}
              activeOpacity={0.7}
            >
              {/* 요일 */}
              <Text style={styles.dayText}>{day}</Text>

              {/* 인원 정보 */}
              <View style={styles.peopleInfo}>
                <Text style={styles.peopleText}>필요인원: 1명</Text>
                <Text style={styles.peopleText}>응답인원: 1명</Text>
              </View>
            </TouchableOpacity>
          ))}
        </ScrollView>

      </View>

      {/* 생성 버튼 */}
      <TouchableOpacity style={styles.createButton}>
        <Text style={styles.createButtonText}>생성</Text>
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
    marginBottom: 24,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /* 기간 */
  period: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
    marginBottom: 16,
  },

  /* 카드 */
  card: {
    backgroundColor: "#fff",
    borderRadius: 30,
    paddingHorizontal: 20,
    paddingVertical: 10,
    elevation: 3,
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    flex: 1,
  },

  /* 요일 row */
  dayRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: "#e5e7eb",
  },
  dayText: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000",
  },

  /* 인원 info */
  peopleInfo: {
    alignItems: "flex-end",
  },
  peopleText: {
    fontSize: 12,
    color: "#000",
    fontWeight: "500",
  },

  /* 생성 버튼 */
  createButton: {
    backgroundColor: "#000",
    borderRadius: 30,
    paddingVertical: 16,
    marginTop: 24,
  },
  createButtonText: {
    textAlign: "center",
    color: "#fff",
    fontSize: 20,
    fontWeight: "bold",
  },
});
