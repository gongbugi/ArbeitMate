import React, { useState, useCallback } from "react";
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from "react-native";
import { Bell, UserCircle, ChevronRight } from "lucide-react-native";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../../services/api"

export default function W_HomeScreen({ navigation }) {
  const [companyName, setCompanyName] = useState("");
  const [expectedSalary, setExpectedSalary] = useState(0);

  // 데이터 로딩 함수
  const fetchData = async () => {
    try {
      // 1. AsyncStorage에서 선택한 근무지 정보 가져오기
      const id = await AsyncStorage.getItem("currentCompanyId");
      const name = await AsyncStorage.getItem("currentCompanyName");
      
      if (name) setCompanyName(name);

      // 2. 이번 달 예상 급여 조회 API 호출
      if (id) {
        const now = new Date();
        const year = now.getFullYear();
        const month = now.getMonth() + 1; // 월은 0부터 시작하므로 +1

        // GET /companies/{companyId}/salary
        const response = await client.get(`/companies/${id}/salary`, {
          params: { year, month },
        });
        
        // SalaryResponse의 totalSalary 필드 사용
        setExpectedSalary(response.data.totalSalary || 0);
      }
    } catch (err) {
      console.log("홈 데이터 로딩 실패:", err);
    }
  };

  // 화면이 포커스될 때마다 데이터 갱신 (다른 화면 갔다가 돌아올 때 등)
  useFocusEffect(
    useCallback(() => {
      fetchData();
    }, [])
  );

  // 금액 포맷팅 (10000 -> 10,000)
  const formatMoney = (amount) => {
    return amount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  };


  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>{companyName}</Text>

        <View style={styles.icons} >
          <TouchableOpacity onPress={() => navigation.navigate("W_InformationScreen")}>
            <UserCircle size={32} color="#000" />
          </TouchableOpacity>
          {/*알림 버튼 클릭 시 공지사항으로 이동(임시)*/}
          <TouchableOpacity onPress={() => navigation.navigate("W_InformationScreen")}>
            <Bell size={28} color="#000" />
          </TouchableOpacity>
        </View>
      </View>

      {/* 공지사항 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_NoticeScreen")}
      >
        <Text style={styles.menuText}>공지사항</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 근무 관리 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_ScheduleManageScreen")}
      >
        <Text style={styles.menuText}>근무 관리</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 근무 가능 시간 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_ScheduleCheckScreen")}
      >
        <Text style={styles.menuText}>근무 가능 시간</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 급여 관리 */}
      
      <TouchableOpacity style={styles.menuCard}
              onPress={() => navigation.navigate("W_PayScreen")}>
                <Text style={styles.menuText}>급여 관리</Text>
                <ChevronRight size={32} color="#999" />
            </TouchableOpacity>

        <View style={styles.salaryBox}>
          <Text style={styles.salaryLabel}>이번 달 예상 급여</Text>
          <Text style={styles.salaryValue}>{formatMoney(expectedSalary)} 원</Text>
        </View>
      

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",
    paddingHorizontal: 24,
    paddingTop: 64,
  },

  /* Header */
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32,
  },
  title: {
    fontSize: 32,
    fontWeight: "bold",
    color: "#000",
  },
  icons: {
    flexDirection: "row",
    gap: 16,
  },

  /* Menu Cards */
  menuCard: {
    backgroundColor: "#fff",
    padding: 20,
    borderRadius: 24,
    marginBottom: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 3,
  },
  menuText: {
    fontSize: 22,
    color: "#000",
  },

  /* Salary Card */
  salaryCard: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    elevation: 3,
    marginTop: 16,
    marginBottom: 40,
  },
  salaryHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,
  },
  salaryTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#000",
  },
  salaryBox: {
    backgroundColor: "#bfdbfe",
    padding: 16,
    borderRadius: 16,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  salaryLabel: {
    fontSize: 16,
    color: "#000",
  },
  salaryValue: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#1d4ed8",
  },
});
