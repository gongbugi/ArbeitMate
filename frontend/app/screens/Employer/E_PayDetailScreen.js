import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_PayDetailScreen({ navigation, route }) {
  const member = route.params.memberData;

  const formatMoney = (amount) =>
    (amount || 0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");

  return (
    <View style={styles.container}>
      {/* HEADER */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>{member.memberName} 급여 상세</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* TOTAL BOX */}
      <View style={styles.totalBox}>
        <View style={styles.rowBetween}>
          <Text style={styles.totalLabel}>총 급여</Text>
          <Text style={styles.totalMoney}>{formatMoney(member.totalSalary)} 원</Text>
        </View>

        <View style={styles.divider} />

        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>기본 급여</Text>
          <Text style={styles.detailValue}>{formatMoney(member.baseSalary)} 원</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>주휴 수당</Text>
          <Text style={styles.detailValue}>+ {formatMoney(member.holidayAllowance)} 원</Text>
        </View>
      </View>

      {/* DETAIL RECORD LIST */}
      <Text style={styles.recordTitle}>근무 기록</Text>

      <FlatList
        data={member.details || []}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({ item }) => (
          <View style={styles.recordItem}>
            <View>
              <Text style={styles.recordLeft}>
                {item.date} ({(item.workMinutes / 60).toFixed(1)}시간)
              </Text>
              <Text style={styles.recordSub}>
                {item.startTime} ~ {item.endTime}
              </Text>
            </View>
            <Text style={styles.recordPrice}>{formatMoney(item.dailySalary)} 원</Text>
          </View>
        )}
      />
    </View>
  );
}

/* ------------- styles ------------- */
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
    color: "#000",
  },
  totalBox: {
    backgroundColor: "#dbeafe",
    borderRadius: 24,
    padding: 24,
    marginBottom: 24,
    elevation: 2,
  },
  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  totalLabel: { fontSize: 18, fontWeight: "600" },
  totalMoney: { fontSize: 24, fontWeight: "bold", color: "#1e40af" },
  divider: {
    height: 1,
    backgroundColor: "rgba(0,0,0,0.2)",
    marginVertical: 12,
  },
  detailRow: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  detailLabel: { fontSize: 15, color: "#555" },
  detailValue: { fontSize: 15, fontWeight: "600" },
  recordTitle: {
    fontSize: 18,
    fontWeight: "600",
    marginBottom: 12,
  },
  recordItem: {
    backgroundColor: "#fff",
    padding: 16,
    marginBottom: 12,
    borderRadius: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    elevation: 1,
  },
  recordLeft: { fontSize: 16, fontWeight: "bold" },
  recordSub: { fontSize: 13, color: "#888" },
  recordPrice: { fontSize: 16, fontWeight: "bold" },
});
