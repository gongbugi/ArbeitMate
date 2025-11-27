import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  Alert
} from "react-native";
import { ArrowLeft, CheckCircle, Circle } from "lucide-react-native"; 
import AsyncStorage from '@react-native-async-storage/async-storage';
import client from "../../services/api";

export default function W_ScheduleRequestScreen({ navigation, route }) {
  const { periodId, periodName } = route.params;

  const [loading, setLoading] = useState(true);
  
  // 데이터를 두 그룹으로 나누어 관리
  const [recommendedSlots, setRecommendedSlots] = useState([]);
  const [otherSlots, setOtherSlots] = useState([]);
  
  const [selectedSlotIds, setSelectedSlotIds] = useState(new Set());

  useEffect(() => {
    fetchSlots();
  }, []);

  const fetchSlots = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      
      const response = await client.get(`/companies/${companyId}/schedule/${periodId}/availability/slots`);
      const data = response.data; 

      // 백엔드에서 이미 나눠준 데이터를 각각 상태에 저장
      setRecommendedSlots(data.recommendedSlots || []);
      setOtherSlots(data.otherSlots || []);

      // 이미 'willing'(가능함)으로 체크된 슬롯들은 선택 상태로 초기화
      const initialSelected = new Set();
      const allSlots = [...(data.recommendedSlots || []), ...(data.otherSlots || [])];
      
      allSlots.forEach(slot => {
        if (slot.willing) {
          initialSelected.add(slot.scheduleId);
        }
      });
      setSelectedSlotIds(initialSelected);

    } catch (err) {
      console.error("슬롯 조회 실패:", err);
      Alert.alert("오류", "데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const toggleSlot = (id) => {
    const newSelected = new Set(selectedSlotIds);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedSlotIds(newSelected);
  };

  const handleSubmit = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      const slotIds = Array.from(selectedSlotIds);

      await client.post(`/companies/${companyId}/schedule/${periodId}/availability/submit`, {
        slotIds: slotIds
      });

      Alert.alert("완료", "근무 가능 시간이 저장되었습니다.", [
        { text: "확인", onPress: () => navigation.goBack() }
      ]);

    } catch (err) {
      console.error("제출 실패:", err);
      Alert.alert("오류", "제출에 실패했습니다.");
    }
  };

  const formatDateTime = (dateStr, startTime, endTime) => {
    const date = new Date(dateStr);
    const mm = date.getMonth() + 1;
    const dd = date.getDate();
    const dayName = ["일", "월", "화", "수", "목", "금", "토"][date.getDay()];
    const timeRange = `${startTime.substring(0, 5)} - ${endTime.substring(0, 5)}`;
    
    return { datePart: `${mm}.${dd} (${dayName})`, timePart: timeRange };
  };

  // 슬롯 리스트를 렌더링하는 헬퍼 함수
  const renderSlotList = (slots) => {
    return slots.map((item) => {
      const { datePart, timePart } = formatDateTime(item.workDate, item.startTime, item.endTime);
      const isSelected = selectedSlotIds.has(item.scheduleId);

      return (
        <TouchableOpacity
          key={item.scheduleId}
          style={[styles.itemRow, isSelected && styles.itemRowSelected]}
          onPress={() => toggleSlot(item.scheduleId)}
          activeOpacity={0.7}
        >
          <View>
            <Text style={[styles.dayText, isSelected && styles.textSelected]}>
              {datePart}
            </Text>
            <Text style={[styles.timeText, isSelected && styles.textSelected]}>
              {timePart} <Text style={styles.roleText}> | {item.roleName}</Text>
            </Text>
          </View>

          {isSelected ? (
            <CheckCircle size={28} color="#3b82f6" fill="#e0f2fe" />
          ) : (
            <Circle size={28} color="#ccc" />
          )}
        </TouchableOpacity>
      );
    });
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#000" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>{periodName}</Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={{ paddingBottom: 100 }}>
        
        {/* === 섹션 1: 추천 근무 시간 (내 근무 패턴에 맞는 시간) === */}
        <Text style={styles.sectionHeader}>추천 근무 시간</Text>
        {recommendedSlots.length > 0 ? (
          renderSlotList(recommendedSlots)
        ) : (
          <View style={styles.emptySection}>
            <Text style={styles.emptyText}>추천 가능한 시간이 없습니다.</Text>
          </View>
        )}

        <View style={{ height: 20 }} />

        {/* === 섹션 2: 그 외 시간 === */}
        <Text style={styles.sectionHeader}>그 외 시간</Text>
        {otherSlots.length > 0 ? (
          renderSlotList(otherSlots)
        ) : (
          <View style={styles.emptySection}>
            <Text style={styles.emptyText}>해당하는 시간이 없습니다.</Text>
          </View>
        )}

      </ScrollView>

      <View style={styles.footer}>
        <TouchableOpacity style={styles.saveButton} onPress={handleSubmit}>
          <Text style={styles.saveText}>제출하기 ({selectedSlotIds.size}개 선택)</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingHorizontal: 24,
    paddingTop: 64,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 16,
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },
  // 섹션 헤더 스타일
  sectionHeader: {
    fontSize: 18,
    fontWeight: "700",
    color: "#333",
    marginTop: 10,
    marginBottom: 10,
  },
  emptySection: {
    padding: 20,
    alignItems: "center",
    backgroundColor: "#fff",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#e5e7eb",
    borderStyle: "dashed",
  },
  emptyText: {
    fontSize: 14,
    color: "#999",
  },
  itemRow: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    borderWidth: 1,
    borderColor: "transparent",
    elevation: 1,
  },
  itemRowSelected: {
    borderColor: "#3b82f6",
    backgroundColor: "#eff6ff", 
  },
  dayText: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#555",
    marginBottom: 4,
  },
  timeText: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },
  textSelected: {
    color: "#1d4ed8",
  },
  roleText: {
    fontSize: 16,
    fontWeight: "normal",
    color: "#666",
  },
  footer: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: "#f4f4f5",
    padding: 24,
  },
  saveButton: {
    backgroundColor: "#000",
    borderRadius: 24,
    paddingVertical: 16,
    alignItems: "center",
    elevation: 3,
  },
  saveText: {
    color: "#fff",
    fontSize: 20,
    fontWeight: "bold",
  },
});