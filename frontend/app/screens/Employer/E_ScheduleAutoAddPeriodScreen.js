import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import PeriodSelectModal from "./E_ScheduleAutoAddPeriodSelectScreen"; // 모달 import

export default function E_ScheduleAutoAddPeriodScreen({ navigation }) {
  const [modalVisible, setModalVisible] = useState(false);
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);

  const formattedPeriod =
    startDate && endDate
      ? `${startDate} ~ ${endDate}`
      : "기간을 선택하세요";

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

      {/* 기간 선택 박스 */}
      <TouchableOpacity
        style={styles.periodBox}
        onPress={() => setModalVisible(true)}
      >
        <Text style={styles.label}>기간</Text>
        <Text style={styles.periodText}>
          {startDate && endDate ? `${startDate} ~ ${endDate}` : "기간을 선택하세요"}
        </Text>
      </TouchableOpacity>

      <Text style={styles.description}>
        자동 생성할 근무표의 시작일과 종료일을 선택하세요.
      </Text>

      {/* Next Button */}
      <TouchableOpacity
        style={[
          styles.nextButton,
          !(startDate && endDate) && styles.disabledButton,
        ]}
        disabled={!(startDate && endDate)}
        onPress={() =>
          navigation.navigate("E_ScheduleAutoAddWeekdayScreen", {
            periodLabel: formattedPeriod,
            startDate,
            endDate,
          })
        }
      >
        <Text style={styles.nextText}>다음</Text>
      </TouchableOpacity>

      {/* 날짜 선택 모달 */}
      <PeriodSelectModal
        visible={modalVisible}
        onClose={() => setModalVisible(false)}
        onSelect={(rangeString) => {
          const [s, e] = rangeString.split(" ~ ");
          setStartDate(s);
          setEndDate(e);
          setModalVisible(false);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 24,
    paddingTop: 60,
    backgroundColor: "#f7f8fa",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 30,
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },
  periodBox: {
    backgroundColor: "#fff",
    padding: 20,
    borderRadius: 20,
    marginBottom: 10,
  },
  label: {
    fontSize: 14,
    color: "#666",
    marginBottom: 8,
  },
  periodText: {
    fontSize: 18,
    fontWeight: "600",
  },
  description: {
    color: "#999",
    marginBottom: 40,
  },
  nextButton: {
    backgroundColor: "#000",
    paddingVertical: 16,
    borderRadius: 12,
  },
  nextText: {
    color: "#fff",
    textAlign: "center",
    fontSize: 16,
    fontWeight: "bold",
  },
  disabledButton: {
    backgroundColor: "#bbb",
  },
});
