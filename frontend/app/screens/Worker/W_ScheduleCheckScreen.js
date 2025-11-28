import React, { useState, useCallback } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  RefreshControl,
  Alert
} from "react-native";
import { ArrowLeft, Trash2, ChevronRight, CheckCircle } from "lucide-react-native"; 
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../../services/api";

export default function W_ScheduleCheckScreen({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [patterns, setPatterns] = useState([]);
  const [requests, setRequests] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  // 데이터 로드 함수
  const fetchData = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      
      const patternRes = await client.get(`/companies/${companyId}/schedule/worker/availability-pattern`);
      const response = await client.get(`/companies/${companyId}/schedule/periods/availability`);
      const periodList = response.data;

      const opens = periodList.filter(p => p.status === "OPEN");
      setRequests(opens);

      // 1. 패턴 데이터 처리
      const rawItems = patternRes.data.items || [];
      const sortedPatterns = rawItems.sort((a, b) => {
        if (a.dow !== b.dow) return a.dow - b.dow;
        return a.startTime.localeCompare(b.startTime);
      });

      // 같은 요일/시간대가 서버에서 중복으로 내려오는 경우가 있어도
      // 화면에는 한 번만 보이도록 클라이언트에서 dedupe
      const uniquePatterns = [];
      const seen = new Set();
      sortedPatterns.forEach((p) => {
        const key = `${p.dow}-${p.startTime}-${p.endTime}-${p.effectiveFrom}-${p.effectiveTo}`;
        if (!seen.has(key)) {
          seen.add(key);
          uniquePatterns.push(p);
        }
      });

      setPatterns(uniquePatterns);
      
    } catch (err) {
      console.log("데이터 로딩 실패:", err);
      // 에러 발생 시 사용자 경험을 위해 Alert 등을 띄울 수 있음
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

  // 패턴 삭제 핸들러
  const handleDelete = async (targetItem) => {
    Alert.alert("삭제 확인", "이 근무 가능 시간을 삭제하시겠습니까?", [
      { text: "취소", style: "cancel" },
      {
        text: "삭제",
        style: "destructive",
        onPress: async () => {
          try {
            const companyId = await AsyncStorage.getItem("currentCompanyId");
            
            const newPatterns = patterns.filter(item => 
              item.memberAvailabilityId !== targetItem.memberAvailabilityId
            );

            const requestBody = {
              items: newPatterns.map(p => ({
                dow: p.dow,
                startTime: p.startTime,
                endTime: p.endTime,
                effectiveFrom: p.effectiveFrom,
                effectiveTo: p.effectiveTo
              }))
            };

            await client.post(`/companies/${companyId}/schedule/worker/availability-pattern`, requestBody);
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
        {/* === 섹션 1: 등록된 패턴  === */}
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
        {/* === 섹션 2: 스케줄 신청 요청 === */}
        <Text style={[styles.sectionTitle, { marginTop: 32 }]}>스케줄 신청 요청</Text>
          {requests.length === 0 ? (
            <View style={styles.emptyBox}>
              <Text style={styles.emptyText}>현재 진행 중인 요청이 없습니다.</Text>
            </View>
          ) : (
            requests.map((req, idx) => (
              <TouchableOpacity 
                key={req.periodId || idx} 
                style={styles.requestBox}
                onPress={() => navigation.navigate("W_ScheduleRequestScreen", { 
                  periodId: req.periodId,
                  periodName: req.name || `${req.startDate} 신청`
                })}
              >
                <View style={styles.requestRow}>
                  <View style={{flex: 1}}>
                    <View style={{flexDirection:'row', alignItems:'center', marginBottom: 4}}>
                      <Text style={styles.requestName}>
                        {req.name && req.name !== "string" ? req.name : "스케줄 신청"}
                      </Text>
                      {/* 제출 완료 여부 표시 (데이터에 submitted 필드가 있다면) */}
                      {req.submitted && (
                        <View style={styles.submittedBadge}>
                          <CheckCircle size={20} color="#15803d" />
                        </View>
                      )}
                    </View>
                    <Text style={styles.requestDate}>
                      {req.startDate} ~ {req.endDate}
                    </Text>
                  </View>
                  <ChevronRight size={24} color="#999" />
                </View>
              </TouchableOpacity>
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
  // 패턴 박스 스타일
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
    backgroundColor: "#f9fafb",
    borderRadius: 12,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: "#e5e7eb"
  },
  emptyText: {
    color: "#999",
    fontSize: 16,
  },
  requestBox: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    elevation: 1,
    borderWidth: 1,
    borderColor: "#e5e7eb",
  },
  requestRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  requestName: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#333",
    marginBottom: 4,
  },
  requestDate: {
    fontSize: 14,
    color: "#666",
  }
});