import React, { useState, useCallback } from "react";
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, ScrollView } from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../../services/api";

export default function W_InformationScreen({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [wage, setWage] = useState(0);
  const [roleName, setRoleName] = useState("미지정"); 
  const [fixedTime, setFixedTime] = useState("정보 없음");

  const fetchInfo = async () => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      // 1. 내 정보 조회 (이름)
      const meRes = await client.get("/members/me");
      setName(meRes.data.name);

      // 2. 담당 업무 조회 (List 반환)
      const roleRes = await client.get(`/companies/${companyId}/me/roles`);
      if (roleRes.data && roleRes.data.length > 0) {
        // 여러 역할이 있을 경우 콤마로 구분하여 표시
        const rolesStr = roleRes.data.map(r => r.name).join(", ");
        setRoleName(rolesStr);
      } else {
        setRoleName("미지정");
      }

      // 3. 급여 정보 조회 (시급)
      const now = new Date();
      const year = now.getFullYear();
      const month = now.getMonth() + 1;

      const salaryRes = await client.get(`/companies/${companyId}/salary`, {
        params: { year, month }
      });
      setWage(salaryRes.data.hourlyWage);

      // 4. 고정 근무 시간 조회
      const fixRes = await client.get(`/companies/${companyId}/me/fixed-shift`);
      const fixData = fixRes.data;

      if (fixData.fixedShiftWorker && fixData.shifts && fixData.shifts.length > 0) {
        // 예: "월 12:00-14:00\n화 12:00-16:00" 형식으로 변환
        const dayMap = ["월", "화", "수", "목", "금", "토", "일"];
        const timeStr = fixData.shifts.map(s => {
          const day = dayMap[s.dow] || "Unknown";
          const start = s.startTime.substring(0, 5);
          const end = s.endTime.substring(0, 5);
          return `${day} ${start}-${end}`;
        }).join("\n");
        setFixedTime(timeStr);
      } else {
        setFixedTime("고정 근무 아님");
      }
      
    } catch (err) {
      console.log("정보 조회 실패:", err);
      Alert.alert("오류", "정보를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      fetchInfo();
    }, [])
  );

  // 금액 포맷 (10000 -> 10,000)
  const formatMoney = (amount) => {
    return (amount || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  };

  if (loading) {
    return (
      <View style={[styles.container, styles.center]}>
        <ActivityIndicator size="large" color="#000" />
      </View>
    );
  }

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>내 정보</Text>

        <View style={{ width: 32 }} />
      </View>

      <ScrollView contentContainerStyle={styles.sectionWrapper}>

        {/* 이름 */}
        <View style={styles.section}>
          <Text style={styles.label}>이름</Text>
          <View style={styles.box}>
            <Text style={styles.valueBlack}>{name}</Text>
          </View>
        </View>

        {/* 담당 업무 */}
        <View style={styles.section}>
          <Text style={styles.label}>담당 업무</Text>
          <View style={styles.box}>
            <Text style={styles.value}>{roleName}</Text>
          </View>
        </View>

        {/* 시급 */}
        <View style={styles.section}>
          <Text style={styles.label}>시급</Text>
          <View style={[styles.box, styles.rowBetween]}>
            <Text style={styles.valueBlack}>{formatMoney(wage)}</Text>
            <Text style={styles.valueBold}>원</Text>
          </View>
        </View>

        {/* 고정 근무 시간 */}
        <View style={styles.section}>
          <Text style={styles.label}>고정 근무 시간</Text>
          <View style={styles.box}>
            <Text style={styles.value}>{fixedTime}</Text>
          </View>
        </View>

        {/* 근무 가능 시간 (화면 이동) */}
        <View style={styles.section}>
          <View style={styles.rowBetween}>
            <Text style={styles.label}>근무 가능 시간</Text>
            <TouchableOpacity onPress={() => navigation.navigate("W_ScheduleCheckScreen")}>
              <ChevronRight size={28} color="#000" />
            </TouchableOpacity>
          </View>

          <TouchableOpacity 
            style={styles.boxLarge}
            onPress={() => navigation.navigate("W_ScheduleCheckScreen")}
          >
            <Text style={styles.linkText}>등록된 근무 가능 시간 확인하기</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>

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
  center: {
    justifyContent: 'center',
    alignItems: 'center',
  },

  /* Header */
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32, 
  },
  headerTitle: {
    fontSize: 20,    
    fontWeight: "bold",
    color: "#000",
  },

  /* Section wrapper */
  sectionWrapper: {
    gap: 24, 
    paddingBottom: 40,
  },

  /* Individual section */
  section: {},

  label: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
  },

  /* White box */
  box: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 14,
    paddingHorizontal: 20,
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
    justifyContent: 'center',
  },

  boxLarge: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 20,
    paddingHorizontal: 20,
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
    alignItems: 'center',
  },

  value: {
    fontSize: 18,
    color: "rgba(0,0,0,0.5)",
  },

  valueBold: {
    fontSize: 18,
    fontWeight: "700",
    color: "rgba(0,0,0,0.8)",
  },

  valueBlack: {
    fontSize: 18,
    fontWeight: "500",
    color: "#000",
  },

  linkText: {
    fontSize: 16,
    color: "#3b82f6",
    fontWeight: "600",
  },

  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
});