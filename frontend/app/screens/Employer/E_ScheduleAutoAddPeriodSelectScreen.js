import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  ScrollView,
  StyleSheet,
} from "react-native";
import { X, ArrowLeft, ArrowRight } from "lucide-react-native";

export default function E_ScheduleAutoAddPeriodSelectScreen({
  visible,
  onClose,
  onSelect,
}) {
  const [currentYear, setCurrentYear] = useState(2025);
  const [currentMonth, setCurrentMonth] = useState(8); // 8월 시작
  const [calendar, setCalendar] = useState([]);

  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);

  // 달력 생성 함수
  const generateCalendar = (year, month) => {
    const start = new Date(year, month - 1, 1);
    const end = new Date(year, month, 0); // 마지막 날

    const startDay = start.getDay(); // 달의 시작 요일
    const totalDays = end.getDate();

    const daysArr = [];
    let week = [];

    // 빈칸 추가
    for (let i = 0; i < startDay; i++) week.push(null);

    // 날짜 넣기
    for (let day = 1; day <= totalDays; day++) {
      week.push(day);
      if (week.length === 7) {
        daysArr.push(week);
        week = [];
      }
    }

    // 남은 칸 null 채움
    while (week.length < 7) week.push(null);
    daysArr.push(week);

    setCalendar(daysArr);
  };

  // month 변경될 때마다 달력 다시 생성
  useEffect(() => {
    generateCalendar(currentYear, currentMonth);
  }, [currentYear, currentMonth]);

  // 월 이동
  const goPrevMonth = () => {
    if (currentMonth === 1) {
      setCurrentYear((prev) => prev - 1);
      setCurrentMonth(12);
    } else {
      setCurrentMonth((prev) => prev - 1);
    }
  };

  const goNextMonth = () => {
    if (currentMonth === 12) {
      setCurrentYear((prev) => prev + 1);
      setCurrentMonth(1);
    } else {
      setCurrentMonth((prev) => prev + 1);
    }
  };

  const handleSelectDate = (day) => {
    if (!startDate) setStartDate(day);
    else if (startDate && !endDate) {
      if (day < startDate) {
        setEndDate(startDate);
        setStartDate(day);
      } else {
        setEndDate(day);
      }
    } else {
      setStartDate(day);
      setEndDate(null);
    }
  };

  // 범위 안인지 체크
  const isInRange = (d) => {
    if (!startDate || !endDate) return false;
    return d > startDate && d < endDate;
  };

  const formatDate = (d) =>
    `${currentYear}-${String(currentMonth).padStart(2, "0")}-${String(d).padStart(2, "0")}`;

  const monthText = `${currentYear}년 ${currentMonth}월`;

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.modalBox}>
          {/* Header */}
          <View style={styles.header}>
            <View>
              <Text style={styles.label}>기간 선택</Text>
              <Text style={styles.selectedDate}>
                {startDate && endDate
                  ? `${formatDate(startDate)} ~ ${formatDate(endDate)}`
                  : startDate
                  ? `${formatDate(startDate)} ~ ?`
                  : "기간을 선택하세요"}
              </Text>
            </View>

            <TouchableOpacity onPress={onClose}>
              <X size={26} color="#444" />
            </TouchableOpacity>
          </View>

          {/* Month Selector */}
          <View style={styles.monthRow}>
            <TouchableOpacity onPress={goPrevMonth}>
              <ArrowLeft size={24} color="#444" />
            </TouchableOpacity>

            <Text style={styles.monthText}>{monthText}</Text>

            <TouchableOpacity onPress={goNextMonth}>
              <ArrowRight size={24} color="#444" />
            </TouchableOpacity>
          </View>

          {/* Week day labels */}
          <View style={styles.weekRow}>
            {["S", "M", "T", "W", "T", "F", "S"].map((d, i) => (
              <Text key={i} style={styles.weekText}>
                {d}
              </Text>
            ))}
          </View>

          {/* Dates */}
          <ScrollView>
            {calendar.map((week, wi) => (
              <View key={wi} style={styles.dateRow}>
                {week.map((d, di) => (
                  <TouchableOpacity
                    key={di}
                    disabled={!d}
                    onPress={() => handleSelectDate(d)}
                    style={[
                    styles.dateCell,
                    d && d === startDate && styles.startDateCell,
                    d && d === endDate && styles.endDateCell,
                    d && isInRange(d) && styles.rangeCell,
                    ]}
                  >
                    <Text
                      style={[
                        styles.dateText,
                        (d === startDate || d === endDate) &&
                          styles.selectedDateText,
                      ]}
                    >
                      {d ?? ""}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            ))}
          </ScrollView>

          {/* Footer */}
          <View style={styles.footer}>
            <TouchableOpacity
              onPress={() => {
                setStartDate(null);
                setEndDate(null);
              }}
            >
              <Text style={styles.clearBtn}>Clear</Text>
            </TouchableOpacity>

            <View style={styles.footerRight}>
              <TouchableOpacity onPress={onClose}>
                <Text style={styles.cancelBtn}>Cancel</Text>
              </TouchableOpacity>

              <TouchableOpacity
                onPress={() => {
                  if (startDate && endDate) {
                    onSelect(
                      `${formatDate(startDate)} ~ ${formatDate(endDate)}`
                    );
                  }
                }}
              >
                <Text style={styles.okBtn}>OK</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.5)",
    justifyContent: "center",
    padding: 24,
  },
  modalBox: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    maxHeight: "85%",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 16,
  },
  label: {
    color: "#666",
    fontSize: 14,
  },
  selectedDate: {
    marginTop: 6,
    fontSize: 18,
    fontWeight: "600",
  },
  monthRow: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 16,
    gap: 20,
  },
  monthText: {
    fontSize: 18,
    fontWeight: "600",
  },
  weekRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  weekText: {
    width: 40,
    textAlign: "center",
    color: "#555",
    fontWeight: "bold",
  },
  dateRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 12,
  },
  dateCell: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: "center",
    alignItems: "center",
  },
  rangeCell: {
    backgroundColor: "#ddd",
  },
  startDateCell: {
    backgroundColor: "#000",
  },
  endDateCell: {
    backgroundColor: "#000",
  },
  dateText: {
    fontSize: 16,
    color: "#000",
  },
  selectedDateText: {
    color: "#fff",
    fontWeight: "bold",
  },
  footer: {
    marginTop: 20,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  clearBtn: {
    fontWeight: "600",
    color: "#000",
  },
  footerRight: {
    flexDirection: "row",
    gap: 16,
  },
  cancelBtn: {
    fontWeight: "600",
    color: "#555",
  },
  okBtn: {
    fontWeight: "600",
    color: "#000",
  },
});
