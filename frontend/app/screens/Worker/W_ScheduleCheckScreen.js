import React, { useState, useCallback } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  RefreshControl,
  Alert
} from "react-native";
import { ArrowLeft, Trash2 } from "lucide-react-native"; // Trash2 아이콘 사용
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../../services/api";

export default function W_ScheduleCheckScreen({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [patterns, setPatterns] = useState([]); 
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      const patternRes = await client.get(`/companies/${companyId}/schedule/worker/availability-pattern`);
      
      // 보기 좋게 정렬 (요일 -> 시작시간 순)
      const sortedItems = (patternRes.data.items || []).sort((a, b) => {
        if (a.dow !== b.dow) return a.dow - b.dow;
        return a.startTime.localeCompare(b.startTime);
      });
      
      setPatterns(sortedItems);
    } catch (err) {
      console.log("데이터 로딩 실패:", err);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      fetchData();
    }, [])
  );

  // 개별 삭제 핸들러
  const handleDelete = async (targetItem) => {
    Alert.alert("삭제 확인", "이 근무 가능 시간을 삭제하시겠습니까?", [
      { text: "취소", style: "cancel" },
      {
        text: "삭제",
        style: "destructive",
        onPress: async () => {
          try {
            const companyId = await AsyncStorage.getItem("currentCompanyId");
            
            // 삭제 대상을 제외한 새 리스트 생성
            const newPatterns = patterns.filter(item => 
              item.memberAvailabilityId !== targetItem.memberAvailabilityId
            );

            // 서버 전송을 위한 데이터 가공 (ID 등 불필요한 필드 제외하고 요청 DTO 형식에 맞춤)
            const requestBody = {
              items: newPatterns.map(p => ({
                dow: p.dow,
                startTime: p.startTime,
                endTime: p.endTime,
                effectiveFrom: p.effectiveFrom,
                effectiveTo: p.effectiveTo
              }))
            };

            // 전체 리스트 업데이트 (덮어쓰기)
            await client.post(`/companies/${companyId}/schedule/worker/availability-pattern`, requestBody);
            
            // 화면 갱신
            setPatterns(newPatterns); 

          } catch (err) {
            console.error("삭제 실패:", err);
            Alert.alert("오류", "삭제에 실패했습니다.");
          }
        }
      }
    ]);
  };

  const dayMap = ["월", "화", "수", "목", "금", "토", "일"];
  const formatTime = (t) => t ? t.substring(0, 5) : "";

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>근무 가능 시간</Text>
        </View>
        <TouchableOpacity onPress={() => navigation.navigate("W_ScheduleAddScreen")}>
          <Text style={styles.addButton}>추가</Text>
        </TouchableOpacity>
      </View>

      <ScrollView 
        contentContainerStyle={{ paddingBottom: 40 }}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); fetchData(); }} />}
      >
        <Text style={styles.sectionTitle}>등록된 시간</Text>
        
        {patterns.length === 0 ? (
          <View style={styles.emptyBox}>
            <Text style={styles.emptyText}>등록된 시간이 없습니다.</Text>
          </View>
        ) : (
          patterns.map((item, idx) => (
            <View key={idx} style={styles.timeBox}>
              <View>
                <Text style={styles.dayText}>{dayMap[item.dow]}요일</Text>
                <Text style={styles.timeText}>
                  {formatTime(item.startTime)} - {formatTime(item.endTime)}
                </Text>
              </View>
              
              <TouchableOpacity onPress={() => handleDelete(item)} hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}>
                <Trash2 size={24} color="#ef4444" />
              </TouchableOpacity>
            </View>
          ))
        )}
      </ScrollView>
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
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    marginLeft: 16,
  },
  addButton: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#3b82f6",
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "600",
    marginBottom: 12,
    color: "#555",
  },
  timeBox: {
    backgroundColor: "#fff",
    borderRadius: 20,
    padding: 20,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 2,
  },
  timeText: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },
  dayText: {
    fontSize: 14,
    fontWeight: "600",
    color: "#666",
    marginBottom: 4,
  },
  emptyBox: {
    padding: 30,
    alignItems: "center",
  },
  emptyText: {
    color: "#999",
    fontSize: 16,
  },
});