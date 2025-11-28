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
import { Calendar, LocaleConfig } from "react-native-calendars"; // 라이브러리 import
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
  const [schedules, setSchedules] = useState([]); // 내 근무 목록 (원본 데이터)
  const [loading, setLoading] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date().toISOString().split('T')[0]); // 현재 보고 있는 달

  // 1. 데이터 가져오기 (급여 API 재활용)
  const fetchMySchedules = async (dateString) => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      // dateString 형식: "2025-11-28" -> year: 2025, month: 11
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = date.getMonth() + 1;

      // [핵심 변경] 급여 API를 통해 "내 근무 기록"을 가져옵니다.
      const response = await client.get(`/companies/${companyId}/salary`, {
        params: { year, month }
      });
      
      const myRecords = response.data.details || [];
      setSchedules(myRecords);
      processMarkedDates(myRecords);

    } catch (err) {
      console.error("내 근무표 로딩 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  // 2. 달력 마킹 데이터 가공
  const processMarkedDates = (records) => {
    const marks = {};
    
    records.forEach(item => {
      // item.date 예시: "2025-11-28"
      marks[item.date] = {
        marked: true, 
        dotColor: '#1e40af', // 파란색 점
        activeOpacity: 0,
      };
    });

    setMarkedDates(marks);
  };

  // 화면 포커스 될 때 데이터 로드
  useFocusEffect(
    useCallback(() => {
      fetchMySchedules(currentMonth);
    }, [currentMonth])
  );

  // 달력 월 변경 시 호출
  const onMonthChange = (month) => {
    setCurrentMonth(month.dateString);
  };

  // 날짜 선택 시 해당 날짜의 근무 정보 필터링
  const selectedSchedules = schedules.filter(s => s.date === selectedDate);

  // 시간 포맷팅 (HH:MM:SS -> HH:MM)
  const formatTime = (timeStr) => timeStr ? timeStr.substring(0, 5) : "";

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>내 근무표</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* Calendar Library */}
      <View style={styles.calendarWrapper}>
        <Calendar
          // 현재 달 설정
          current={currentMonth}
          // 월 변경 콜백
          onMonthChange={onMonthChange}
          // 마킹된 날짜들 (점 표시)
          markedDates={{
            ...markedDates,
            [selectedDate]: { 
              selected: true, 
              marked: markedDates[selectedDate]?.marked, 
              selectedColor: '#3b82f6' // 선택된 날짜 파란 배경
            }
          }}
          // 날짜 선택 핸들러
          onDayPress={(day) => setSelectedDate(day.dateString)}
          
          // 스타일 커스터마이징
          theme={{
            todayTextColor: '#3b82f6',
            arrowColor: 'black',
            textDayFontWeight: '600',
            textMonthFontWeight: 'bold',
            textDayHeaderFontWeight: 'bold',
            textDayFontSize: 16,
            textMonthFontSize: 20,
            textDayHeaderFontSize: 14
          }}
        />
      </View>

      {/* 상세 근무 정보 표시 영역 */}
      <View style={styles.detailContainer}>
        <Text style={styles.detailTitle}>
          {selectedDate ? `${selectedDate} 근무 일정` : "날짜를 선택하세요"}
        </Text>

        {loading ? (
          <ActivityIndicator color="#000" style={{ marginTop: 20 }} />
        ) : (
          <ScrollView showsVerticalScrollIndicator={false}>
            {selectedSchedules.length > 0 ? (
              selectedSchedules.map((item, idx) => (
                <View key={idx} style={styles.scheduleItem}>
                  <View style={styles.timeBadge}>
                    <Text style={styles.timeText}>
                      {formatTime(item.startTime)} ~ {formatTime(item.endTime)}
                    </Text>
                  </View>
                  <Text style={styles.durationText}>
                    {(item.workMinutes / 60).toFixed(1)}시간 근무
                  </Text>
                  <Text style={styles.salaryText}>
                    예상 급여: {item.dailySalary.toLocaleString()}원
                  </Text>
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
  timeText: {
    color: '#1e40af',
    fontWeight: 'bold',
    fontSize: 14,
  },
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
  emptyText: {
    color: '#94a3b8',
    textAlign: 'center',
    marginTop: 20,
    fontSize: 16,
  }
});