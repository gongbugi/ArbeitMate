import React, { useEffect, useState } from "react";
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from "react-native";
import { ChevronRight, Bell, Home } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function E_HomeScreen({ navigation }) {
  const [unpaid, setUnpaid] = useState(0);

  useEffect(() => {
    loadUnpaidSalary();
  }, []);

  /*  지급해야 할 금액 불러오기 */
  const loadUnpaidSalary = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      
      const now = new Date();
      const year = now.getFullYear();
      const month = now.getMonth() + 1;

      const res = await client.get(`/companies/${companyId}/salary`, {
        params: { year, month }
      });

      // 실제 필드명에 따라 맞추면 됨
      setUnpaid(res.data?.unpaidSalaryTotal ?? 0);

    } catch (err) {
      console.log("급여 요약 조회 실패:", err?.response || err);
      setUnpaid(0);
    }
  };

  /** 금액 1,000 단위 포맷 */
  const formatMoney = (num) =>
    (num || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>근무지 1</Text>

        <View style={styles.headerIcons}>
          <TouchableOpacity onPress={() => navigation.navigate("E_InformationScreen")}>
            <Home size={32} color="#000" />
          </TouchableOpacity>

          <TouchableOpacity>
            <Bell size={32} color="#000" />
          </TouchableOpacity>
        </View>
      </View>

      {/* Scroll */}
      <ScrollView showsVerticalScrollIndicator={false}>

        {/* 공지사항 */}
        <TouchableOpacity style={styles.menuCard}
          onPress={() => navigation.navigate("E_NoticeScreen")}
        >
          <Text style={styles.menuText}>공지사항</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 근무 관리 */}
        <TouchableOpacity style={styles.menuCard}
          onPress={() => navigation.navigate("E_ScheduleManageScreen")}
        >
          <Text style={styles.menuText}>근무 관리</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 근무자 */}
        <TouchableOpacity style={styles.menuCard}
          onPress={() => navigation.navigate("E_WorkerManageScreen")}
        >
          <Text style={styles.menuText}>근무자</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 급여 관리 (맨 아래 메뉴) */}
        <TouchableOpacity style={styles.menuCard}
          onPress={() => navigation.navigate("E_PayListScreen")}
        >
          <Text style={styles.menuText}>급여 관리</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 지급해야 할 돈 요약 박스 */}
        <View style={styles.moneySummaryBox}>
          <Text style={styles.moneyTitle}>지급해야 할 돈</Text>
          <Text style={styles.moneyAmount}>- {formatMoney(unpaid)} 원</Text>
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
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: "bold",
  },
  headerIcons: {
    flexDirection: "row",
    gap: 16,
  },
  menuCard: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    marginBottom: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 3,
  },
  menuText: {
    fontSize: 22,
  },

  /** 요약 박스 */
  moneySummaryBox: {
    backgroundColor: "#fee2e2",
    padding: 20,
    borderRadius: 16,
    marginBottom: 40,
  },
  moneyTitle: {
    fontSize: 18,
    color: "#b91c1c",
    fontWeight: "600",
  },
  moneyAmount: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#dc2626",
    marginTop: 4,
    textAlign: "right",
  },
});
