import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  FlatList,
  StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ScheduleAutoAddScreen({ navigation, route }) {
  const [requests, setRequests] = useState([]);

  /* WeekdayScreen → 돌아올 때 전달되는 데이터 처리 */
  useEffect(() => {
    if (route.params?.weekdayConfigs) {
      const {
        periodId,
        startDate,
        endDate,
        periodLabel,
        weekdayConfigs,
      } = route.params;

      const newRequest = {
        id: Date.now().toString(),
        periodId,
        startDate,
        endDate,
        periodLabel,
        weekdayConfigs,
      };

      setRequests((prev) => [...prev, newRequest]);
    }
  }, [route.params]);

  /* Summary 이동 */
  const goToSummary = (item) => {
    navigation.navigate("E_ScheduleAutoAddSummaryScreen", {
      periodId: item.periodId,
      startDate: item.startDate,
      endDate: item.endDate,
      periodLabel: item.periodLabel,
      weekdayConfigs: item.weekdayConfigs,
    });
  };

  /* 보낸 요청 카드 */
  const renderItem = ({ item }) => (
    <TouchableOpacity style={styles.card} onPress={() => goToSummary(item)}>
      <View style={{ flexDirection: "row", justifyContent: "space-between" }}>
        <Text style={styles.dateText}>{item.periodLabel}</Text>
        <Text style={{ fontSize: 20 }}>›</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* 헤더 */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무표 자동 생성</Text>
        <View style={{ width: 28 }} />
      </View>

      {/* 요청 작성 버튼 */}
      <TouchableOpacity
        style={styles.createButton}
        onPress={() => navigation.navigate("E_ScheduleAutoAddPeriodScreen")}
      >
        <Text style={styles.createButtonText}>요청 작성</Text>
      </TouchableOpacity>

      {/* 보낸 요청 */}
      <Text style={styles.sectionTitle}>보낸 요청</Text>

      <FlatList
        data={requests}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        ListEmptyComponent={
          <Text style={styles.emptyText}>보낸 요청이 없습니다.</Text>
        }
      />
    </View>
  );
}

/* 스타일 */
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
    alignItems: "center",
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },

  /* 요청 작성 버튼 */
  createButton: {
    backgroundColor: "#4A67FF",
    paddingVertical: 14,
    borderRadius: 12,
    marginBottom: 20,
  },
  createButtonText: {
    color: "#fff",
    textAlign: "center",
    fontSize: 16,
    fontWeight: "bold",
  },

  /* 보낸 요청 */
  sectionTitle: {
    fontSize: 16,
    fontWeight: "bold",
    marginBottom: 12,
  },
  card: {
    backgroundColor: "#fff",
    padding: 16,
    borderRadius: 16,
    marginBottom: 12,
    elevation: 2,
  },
  dateText: {
    fontSize: 16,
    fontWeight: "600",
  },
  emptyText: {
    marginTop: 20,
    textAlign: "center",
    color: "#aaa",
  },
});
