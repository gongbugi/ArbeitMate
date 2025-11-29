import React, { useEffect, useMemo, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

const DAYS = ["월", "화", "수", "목", "금", "토", "일"];


const parseDate = (str) => {
  const [y, m, d] = str.split("-").map((v) => parseInt(v, 10));
  return new Date(y, m - 1, d);
};


const formatDate = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

// start ~ end 사이에서 특정 요일에 해당하는 날짜 리스트
const getDatesForWeekday = (startDateStr, endDateStr, weekdayIndex) => {
  const start = parseDate(startDateStr);
  const end = parseDate(endDateStr);
  const result = [];

  const d = new Date(start);
  while (d <= end) {
    // JS 요일: 일=0 ~ 토=6  / 우리 요일 index: 월=0 ~ 일=6
    const jsDay = d.getDay(); // 0~6
    const idx = jsDay === 0 ? 6 : jsDay - 1;
    if (idx === weekdayIndex) {
      result.push(formatDate(d));
    }
    d.setDate(d.getDate() + 1);
  }
  return result;
};

const ensureSeconds = (time) => {
  // "HH:MM" -> "HH:MM:00"
  if (!time) return time;
  if (time.length === 5) return `${time}:00`;
  return time;
};

export default function E_ScheduleAutoAddSummaryScreen({ navigation, route }) {
  const { periodId, startDate, endDate, periodLabel, weekdayConfigs } =
    route.params;

  const [respondedCount, setRespondedCount] = useState(0);
  const [creating, setCreating] = useState(false);
  const [loadingSubmissions, setLoadingSubmissions] = useState(false);

  // 필요한 총 인원 (패턴 * 해당 요일 날짜 수)
  const requiredTotal = useMemo(() => {
    let total = 0;
    Object.entries(weekdayConfigs || {}).forEach(([idxStr, list]) => {
      const weekdayIndex = parseInt(idxStr, 10);
      const dates = getDatesForWeekday(startDate, endDate, weekdayIndex);
      const multiplier = dates.length;
      list.forEach((p) => {
        total += (p.requiredHeadCount || 0) * multiplier;
      });
    });
    return total;
  }, [weekdayConfigs, startDate, endDate]);

  const loadSubmissions = async () => {
    try {
      setLoadingSubmissions(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      const res = await client.get(
        `/companies/${companyId}/schedule/${periodId}/availability/submissions`
      );

      const list = res.data || [];
      const count = list.filter((x) => x.submitted).length;
      setRespondedCount(count);
    } catch (err) {
      console.log("응답 인원 조회 실패:", err?.response ?? err);
    } finally {
      setLoadingSubmissions(false);
    }
  };

  useEffect(() => {
    loadSubmissions();
  }, []);

  const handleCreateSchedule = async () => {
    if (!requiredTotal) {
      alert("설정된 근무 패턴이 없습니다.");
      return;
    }

    try {
      setCreating(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) {
        alert("회사 정보가 없습니다.");
        return;
      }

      // 1) slots 생성
      const slots = [];
      Object.entries(weekdayConfigs || {}).forEach(([idxStr, list]) => {
        const weekdayIndex = parseInt(idxStr, 10);
        const dates = getDatesForWeekday(startDate, endDate, weekdayIndex);

        dates.forEach((dateStr) => {
          list.forEach((p) => {
            slots.push({
              roleId: p.roleId,
              workDate: dateStr,
              startTime: ensureSeconds(p.startTime),
              endTime: ensureSeconds(p.endTime),
              requiredHeadCount: p.requiredHeadCount,
            });
          });
        });
      });

      if (!slots.length) {
        alert("생성할 슬롯이 없습니다.");
        return;
      }

      await client.post(
        `/companies/${companyId}/schedule/${periodId}/create/slots`,
        { slots }
      );

      // 2) 자동 배정
      await client.post(
        `/companies/${companyId}/schedule/${periodId}/auto-assign`
      );

      alert("근무표가 생성되고 자동 배정이 완료되었습니다.");
      navigation.goBack();
    } catch (err) {
      console.log("근무표 생성 실패:", err?.response ?? err);
      alert("근무표 생성 중 오류가 발생했습니다.");
    } finally {
      setCreating(false);
    }
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

      {/* 기간 */}
      <Text style={styles.periodText}>{periodLabel}</Text>

      {/* 요약 카드 */}
      <View style={styles.card}>
        <Text style={styles.summaryLabel}>필요 인원</Text>
        <Text style={styles.summaryValue}>{requiredTotal}명</Text>

        <View style={styles.divider} />

        <Text style={styles.summaryLabel}>응답 인원</Text>
        {loadingSubmissions ? (
          <ActivityIndicator style={{ marginTop: 4 }} />
        ) : (
          <Text style={styles.summaryValue}>{respondedCount}명</Text>
        )}
      </View>

      {/* 생성 버튼 */}
      <TouchableOpacity
        style={styles.createButton}
        onPress={handleCreateSchedule}
        disabled={creating}
      >
        {creating ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <Text style={styles.createButtonText}>생성</Text>
        )}
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
  card: {
    marginTop: 24,
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingHorizontal: 24,
    paddingVertical: 32,
  },
  summaryLabel: {
    fontSize: 16,
    color: "#4b5563",
  },
  summaryValue: {
    marginTop: 8,
    fontSize: 22,
    fontWeight: "bold",
  },
  divider: {
    height: 1,
    backgroundColor: "#e5e7eb",
    marginVertical: 20,
  },
  createButton: {
    marginTop: 32,
    backgroundColor: "#000",
    borderRadius: 30,
    height: 56,
    alignItems: "center",
    justifyContent: "center",
  },
  createButtonText: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "bold",
  },
});