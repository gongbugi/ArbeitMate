import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

const DAYS = ["월", "화", "수", "목", "금", "토", "일"];

export default function E_ScheduleAutoAddWeekdayScreen({ navigation, route }) {
  const { periodId, startDate, endDate, periodLabel } = route.params;

  // weekdayConfigs: { [weekdayIndex: number]: Pattern[] }
  const [weekdayConfigs, setWeekdayConfigs] = useState({});

  const handleOpenDay = (index) => {
    const dayLabel = DAYS[index];

    navigation.navigate("E_ScheduleAutoAddPeopleScreen", {
      weekdayIndex: index,
      weekdayLabel: dayLabel,
      periodId,
      startDate,
      endDate,
      patterns: weekdayConfigs[index] || [],
      // 콜백으로 되돌려 받기
      onSave: (weekdayIdx, updatedList) => {
        setWeekdayConfigs((prev) => ({
          ...prev,
          [weekdayIdx]: updatedList,
        }));
      },
    });
  };

  const handleGoSummary = () => {
    navigation.navigate("E_ScheduleAutoAddScreen", {
      periodId,
      startDate,
      endDate,
      periodLabel,
      weekdayConfigs,
    });
  };

  const getRequiredCountForDay = (index) => {
    const list = weekdayConfigs[index] || [];
    if (!list.length) return 0;
    return list.reduce((sum, p) => sum + (p.requiredHeadCount || 0), 0);
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무표 자동 생성</Text>
        <View style={{ width: 28 }} />
      </View>

      {/* 기간 표시 */}
      <Text style={styles.periodText}>{periodLabel}</Text>

      {/* 요일 리스트 */}
      <ScrollView contentContainerStyle={{ paddingVertical: 16 }}>
        {DAYS.map((day, idx) => {
          const count = getRequiredCountForDay(idx);
          return (
            <TouchableOpacity
              key={day}
              style={styles.dayCard}
              onPress={() => handleOpenDay(idx)}
            >
              <View>
                <Text style={styles.dayLabel}>{day}</Text>
                <Text style={styles.subText}>
                  {count > 0 ? `필요 인원: ${count}명` : "설정되지 않음"}
                </Text>
              </View>
              <ChevronRight size={22} color="#9ca3af" />
            </TouchableOpacity>
          );
        })}
      </ScrollView>

      {/* 저장(요약으로 이동) 버튼 */}
      <TouchableOpacity style={styles.saveBtn} onPress={handleGoSummary}>
        <Text style={styles.saveText}>저장</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",
    paddingTop: 64,
    paddingHorizontal: 20,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },
  periodText: {
    marginTop: 16,
    fontSize: 14,
    color: "#6b7280",
  },
  dayCard: {
    marginTop: 16,
    backgroundColor: "#fff",
    borderRadius: 20,
    paddingVertical: 16,
    paddingHorizontal: 20,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  dayLabel: {
    fontSize: 18,
    fontWeight: "bold",
  },
  subText: {
    marginTop: 4,
    color: "#6b7280",
    fontSize: 13,
  },
  saveBtn: {
    backgroundColor: "#000",
    height: 56,
    borderRadius: 28,
    alignItems: "center",
    justifyContent: "center",
    marginVertical: 20,
  },
  saveText: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#fff",
  },
});