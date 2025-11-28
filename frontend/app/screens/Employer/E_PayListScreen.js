import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  FlatList,
} from "react-native";
import { ArrowLeft, ArrowRight } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function E_PayListScreen({ navigation }) {
  const [loading, setLoading] = useState(false);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [salaryList, setSalaryList] = useState([]);

  useEffect(() => {
    fetchEmployerSalary();
  }, [currentDate]);

  const fetchEmployerSalary = async () => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      const year = currentDate.getFullYear();
      const month = currentDate.getMonth() + 1;

      // 📌 API: (예시)
      // GET /companies/{companyId}/salary/all?year=2025&month=10
      // 실제 백엔드 API 이름에 맞춰 수정 필요
      const res = await client.get(
        `/companies/${companyId}/salary/all`,
        { params: { year, month } }
      );

      setSalaryList(res.data);

    } catch (err) {
      console.log("급여 조회 실패:", err);
      setSalaryList([]);
    } finally {
      setLoading(false);
    }
  };

  const handlePrevMonth = () => {
    const newDate = new Date(currentDate);
    newDate.setMonth(newDate.getMonth() - 1);
    setCurrentDate(newDate);
  };

  const handleNextMonth = () => {
    const newDate = new Date(currentDate);
    newDate.setMonth(newDate.getMonth() + 1);
    setCurrentDate(newDate);
  };

  const formatMoney = (amount) =>
    (amount || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.card}
      onPress={() =>
        navigation.navigate("E_PayDetailScreen", { memberData: item })
      }
    >
      <View>
        <Text style={styles.memberName}>{item.memberName}</Text>
        <Text style={styles.subText}>기본급: {formatMoney(item.baseSalary)} 원</Text>
        <Text style={styles.subText}>주휴수당: +{formatMoney(item.holidayAllowance)} 원</Text>
      </View>

      <Text style={styles.totalPay}>{formatMoney(item.totalSalary)} 원</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* HEADER */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>직원 급여 관리</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* MONTH SELECTOR */}
      <View style={styles.monthSelector}>
        <TouchableOpacity onPress={handlePrevMonth}>
          <ArrowLeft size={24} color="#000" />
        </TouchableOpacity>

        <Text style={styles.monthText}>
          {currentDate.getFullYear()}년 {currentDate.getMonth() + 1}월
        </Text>

        <TouchableOpacity onPress={handleNextMonth}>
          <ArrowRight size={24} color="#000" />
        </TouchableOpacity>
      </View>

      {/* DATA */}
      {loading ? (
        <ActivityIndicator size="large" color="#000" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={salaryList}
          renderItem={renderItem}
          keyExtractor={(item, idx) => idx.toString()}
          contentContainerStyle={{ paddingBottom: 30 }}
          ListEmptyComponent={
            <Text style={{ textAlign: "center", color: "#aaa", marginTop: 20 }}>
              급여 데이터가 없습니다.
            </Text>
          }
        />
      )}
    </View>
  );
}

/* ------------------ STYLES ------------------ */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingHorizontal: 24,
    paddingTop: 64,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 20,
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },
  monthSelector: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 16,
    marginBottom: 24,
  },
  monthText: {
    fontSize: 20,
    fontWeight: "700",
  },
  card: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    elevation: 2,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  memberName: {
    fontSize: 18,
    fontWeight: "bold",
  },
  subText: {
    fontSize: 13,
    color: "#6b7280",
  },
  totalPay: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#1e40af",
  },
});
