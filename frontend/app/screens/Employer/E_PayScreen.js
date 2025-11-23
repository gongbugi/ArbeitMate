import React from "react";
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from "react-native";
import { ArrowLeft, ArrowRight } from "lucide-react-native";

export default function E_PayScreen({ navigation }) {
  const records = [
    { name: "이XX", date: "10.28 (2시간)", pay: "25,000 원" },
    { name: "이XX", date: "10.21 (2시간)", pay: "25,000 원" },
    { name: "이XX", date: "10.14 (2시간)", pay: "25,000 원" },
    { name: "이XX", date: "10.07 (2시간)", pay: "25,000 원" },
    { name: "김XX", date: "10.26 (2시간)", pay: "25,000 원" },
    { name: "김XX", date: "10.19 (2시간)", pay: "25,000 원" },
    { name: "김XX", date: "10.12 (2시간)", pay: "25,000 원" },
    { name: "김XX", date: "10.05 (2시간)", pay: "25,000 원" },
  ];

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>급여 관리</Text>
        <View style={{ width: 28 }} />
      </View>

      {/* Month Selector */}
      <View style={styles.monthSelector}>
        <ArrowLeft size={22} color="#000" />
        <Text style={styles.monthText}>10월</Text>
        <ArrowRight size={22} color="#000" />
      </View>

      {/* Total Money */}
      <View style={styles.totalBox}>
        <Text style={styles.totalLabel}>총 금액</Text>
        <Text style={styles.totalValue}>200,000 원</Text>
      </View>

      {/* Records Title */}
      <Text style={styles.recordTitle}>기록</Text>

      {/* Record List */}
      <ScrollView showsVerticalScrollIndicator={false} style={{ marginTop: 10 }}>
        {records.map((item, idx) => (
          <View key={idx} style={styles.recordItem}>
            <View>
              <Text style={styles.recordName}>{item.name}</Text>
              <Text style={styles.recordDate}>{item.date}</Text>
            </View>
            <Text style={styles.recordPay}>{item.pay}</Text>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F3F4F6",
    paddingHorizontal: 24,
    paddingTop: 60,
  },

  // Header
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
  },

  // Month
  monthSelector: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginTop: 20,
  },
  monthText: {
    fontSize: 20,
    fontWeight: "bold",
    marginHorizontal: 14,
  },

  // Total Box
  totalBox: {
    backgroundColor: "#FECACA",
    borderRadius: 20,
    paddingVertical: 25,
    paddingHorizontal: 20,
    alignItems: "center",
    marginTop: 24,
  },
  totalLabel: {
    fontSize: 16,
    fontWeight: "500",
  },
  totalValue: {
    fontSize: 32,
    fontWeight: "bold",
    marginTop: 8,
  },

  // Records
  recordTitle: {
    marginTop: 28,
    fontSize: 20,
    fontWeight: "bold",
  },

  recordItem: {
    backgroundColor: "#FFF",
    padding: 18,
    borderRadius: 18,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    elevation: 2,
  },
  recordName: {
    fontSize: 20,
    fontWeight: "bold",
  },
  recordDate: {
    marginTop: 4,
    fontSize: 14,
    color: "#777",
  },
  recordPay: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    alignSelf: "center",
  },
});
