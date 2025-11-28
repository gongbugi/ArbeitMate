import React, { useState, useCallback } from "react";
import { 
  View, 
  Text, 
  TouchableOpacity, 
  StyleSheet, 
  ActivityIndicator,
  ScrollView
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import { Calendar, LocaleConfig } from "react-native-calendars";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../services/api";

// 한국어 설정
LocaleConfig.locales['kr'] = {
  monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
  monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
  dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
  dayNamesShort: ['일','월','화','수','목','금','토'],
  today: '오늘'
};
LocaleConfig.defaultLocale = 'kr';

export default function ScheduleScreen({ navigation }) {
  const [markedDates, setMarkedDates] = useState({});
  const [selectedDate, setSelectedDate] = useState("");
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date().toISOString().split('T')[0]);
  
  const [isEmployer, setIsEmployer] = useState(false); // 역할 상태 관리

  // 1. 초기 설정: 내 역할 확인 및 데이터 로드
  useFocusEffect(
    useCallback(() => {
      checkRoleAndFetch(currentMonth);
    }, [currentMonth])
  );

  const checkRoleAndFetch = async (dateString) => {
    setLoading(true);
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      // 1-1. 내 역할 조회
      // (매번 호출하는 게 부담스럽다면 최초 1회만 호출해서 state에 저장하거나 AsyncStorage에 저장된 값을 써도 됨)
      const companiesRes = await client.get("/companies/me");
      const myCompany = companiesRes.data.find(c => c.companyId === companyId);
      const role = myCompany ? myCompany.role : "WORKER";
      const isOwner = role === "OWNER";
      
      setIsEmployer(isOwner);

      // 1-2. 역할에 따라 다른 데이터 로드 함수 호출
      if (isOwner) {
        await fetchAllSchedules(companyId, dateString);
      } else {
        await fetchMySchedules(companyId, dateString);
      }

    } catch (err) {
      console.error("데이터 로딩 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // [고용주용] 모든 스케줄 조회
  const fetchAllSchedules = async (companyId, dateString) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;

    const response = await client.get(`/companies/${companyId}/schedule/monthly`, {
      params: { year, month }
    });

    const allRecords = response.data; // List<ScheduleSlotResponse>
    setSchedules(allRecords);
    
    // 달력 마킹 (근무가 하나라도 있는 날 표시)
    const marks = {};
    allRecords.forEach(item => {
      // item.workDate 예시: "2025-11-28"
      marks[item.workDate] = {
        marked: true, 
        dotColor: '#10b981', // 고용주는 초록색 점
      };
    });
    setMarkedDates(marks);
  };

  // [근무자용] 내 근무 조회 (급여 API 활용)
  const fetchMySchedules = async (companyId, dateString) => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;

    const response = await client.get(`/companies/${companyId}/salary`, {
      params: { year, month }
    });
    
    const myRecords = response.data.details || [];
    setSchedules(myRecords);

    // 달력 마킹
    const marks = {};
    myRecords.forEach(item => {
      marks[item.date] = {
        marked: true, 
        dotColor: '#1e40af', // 근무자는 파란색 점
      };
    });
    setMarkedDates(marks);
  };

  const onMonthChange = (month) => {
    setCurrentMonth(month.dateString);
  };

  // 선택된 날짜의 데이터 필터링
  const getSelectedItems = () => {
    if (!selectedDate) return [];
    if (isEmployer) {
      // 고용주: workDate 기준 필터링
      return schedules.filter(s => s.workDate === selectedDate);
    } else {
      // 근무자: date 기준 필터링
      return schedules.filter(s => s.date === selectedDate);
    }
  };

  const selectedItems = getSelectedItems();
  const formatTime = (timeStr) => timeStr ? timeStr.substring(0, 5) : "";

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>{isEmployer ? "전체 근무표" : "내 근무표"}</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* Calendar */}
      <View style={styles.calendarWrapper}>
        <Calendar
          current={currentMonth}
          onMonthChange={onMonthChange}
          markedDates={{
            ...markedDates,
            [selectedDate]: { 
              selected: true, 
              marked: markedDates[selectedDate]?.marked, 
              selectedColor: isEmployer ? '#10b981' : '#3b82f6' 
            }
          }}
          onDayPress={(day) => setSelectedDate(day.dateString)}
          theme={{
            todayTextColor: isEmployer ? '#10b981' : '#3b82f6',
            arrowColor: 'black',
            textDayFontWeight: '600',
            textMonthFontWeight: 'bold',
            textDayHeaderFontWeight: 'bold',
            textDayFontSize: 16,
            textMonthFontSize: 20,
          }}
        />
      </View>

      {/* Details List */}
      <View style={styles.detailContainer}>
        <Text style={styles.detailTitle}>
          {selectedDate ? `${selectedDate} 근무 일정` : "날짜를 선택하세요"}
        </Text>

        {loading ? (
          <ActivityIndicator color="#000" style={{ marginTop: 20 }} />
        ) : (
          <ScrollView showsVerticalScrollIndicator={false}>
            {selectedItems.length > 0 ? (
              selectedItems.map((item, idx) => (
                <View key={idx} style={styles.scheduleItem}>
                  <View style={[styles.timeBadge, isEmployer && styles.timeBadgeEmployer]}>
                    <Text style={[styles.timeText, isEmployer && styles.timeTextEmployer]}>
                      {formatTime(item.startTime)} ~ {formatTime(item.endTime)}
                    </Text>
                  </View>

                  {/* 고용주 vs 근무자 표시 내용 다르게 처리 */}
                  {isEmployer ? (
                    // 고용주 화면: 역할, 필요인원 표시
                    <View>
                      <Text style={styles.roleText}>{item.roleName || "역할 없음"}</Text>
                      <Text style={styles.subText}>필요 인원: {item.requiredHeadCount}명</Text>
                    </View>
                  ) : (
                    // 근무자 화면: 근무 시간, 급여 표시
                    <View>
                      <Text style={styles.durationText}>
                        {(item.workMinutes / 60).toFixed(1)}시간 근무
                      </Text>
                      <Text style={styles.salaryText}>
                        예상 급여: {item.dailySalary?.toLocaleString()}원
                      </Text>
                    </View>
                  )}
                </View>
              ))
            ) : (
              selectedDate && (
                <Text style={styles.emptyText}>해당 날짜에 근무가 없습니다.</Text>
              )
            )}
          </ScrollView>
        )}
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingTop: 64,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 24,
    marginBottom: 20,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
  },
  calendarWrapper: {
    marginHorizontal: 20,
    borderRadius: 20,
    overflow: 'hidden',
    elevation: 3,
    backgroundColor: 'white',
    paddingBottom: 10
  },
  detailContainer: {
    flex: 1,
    marginTop: 24,
    backgroundColor: '#fff',
    borderTopLeftRadius: 30,
    borderTopRightRadius: 30,
    padding: 24,
    elevation: 10,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -2 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
  },
  detailTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
    color: '#333'
  },
  scheduleItem: {
    backgroundColor: '#f8fafc',
    padding: 16,
    borderRadius: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  timeBadge: {
    backgroundColor: '#dbeafe',
    alignSelf: 'flex-start',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    marginBottom: 8,
  },
  timeBadgeEmployer: {
    backgroundColor: '#d1fae5', // 고용주는 초록색 뱃지
  },
  timeText: {
    color: '#1e40af',
    fontWeight: 'bold',
    fontSize: 14,
  },
  timeTextEmployer: {
    color: '#065f46',
  },
  // 근무자용 스타일
  durationText: {
    fontSize: 16,
    color: '#333',
    fontWeight: '600',
    marginBottom: 4,
  },
  salaryText: {
    fontSize: 14,
    color: '#64748b',
  },
  // 고용주용 스타일
  roleText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#111',
    marginBottom: 4,
  },
  subText: {
    fontSize: 14,
    color: '#666',
  },
  emptyText: {
    color: '#94a3b8',
    textAlign: 'center',
    marginTop: 20,
    fontSize: 16,
  }
});