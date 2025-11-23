import React from "react";
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from "react-native";
import { ArrowLeft, ArrowRight } from "lucide-react-native";

export default function W_PayScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Back + Title */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}
        onPress={() => navigation.navigate("W_PayScreen")}>급여 관리</Text>
      </View>

      {/* Month Selector */}
      <View style={styles.monthSelector}>
        <ArrowLeft size={22} color="#000" />
        <Text style={styles.monthText}>10월</Text>
        <ArrowRight size={22} color="#000" />
      </View>

      {/* Total Payment */}
      <View style={styles.totalBox}>
        <Text style={styles.totalLabel}>총 금액</Text>
        <Text style={styles.totalMoney}>100,000 원</Text>
      </View>

      {/* Record Title */}
      <Text style={styles.recordTitle}>기록</Text>

      {/* Record List */}
      <ScrollView style={styles.recordList}>
        
        {/** 반복 가능하도록 데이터 배열로 리팩토링 가능 */}
        <View style={styles.recordItem}>
          <Text style={styles.recordLeft}>
            10.26 <Text style={styles.recordTime}>(2시간)</Text>
          </Text>
          <Text style={styles.recordPrice}>25,000 원</Text>
        </View>

        <View style={styles.recordItem}>
          <Text style={styles.recordLeft}>
            10.19 <Text style={styles.recordTime}>(2시간)</Text>
          </Text>
          <Text style={styles.recordPrice}>25,000 원</Text>
        </View>

        <View style={styles.recordItem}>
          <Text style={styles.recordLeft}>
            10.12 <Text style={styles.recordTime}>(2시간)</Text>
          </Text>
          <Text style={styles.recordPrice}>25,000 원</Text>
        </View>

        <View style={styles.recordItem}>
          <Text style={styles.recordLeft}>
            10.05 <Text style={styles.recordTime}>(2시간)</Text>
          </Text>
          <Text style={styles.recordPrice}>25,000 원</Text>
        </View>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",  // zinc-100
    paddingHorizontal: 20,
    paddingTop: 64,
  },

  /** Header */
  header: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /** Month Selector */
  monthSelector: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    marginTop: 24,
  },
  monthText: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginHorizontal: 20,
  },

  /** Total Payment Box */
  totalBox: {
    backgroundColor: "#dbeafe",  // blue-100
    borderRadius: 20,
    paddingVertical: 20,
    paddingHorizontal: 20,
    marginTop: 24,
    alignItems: "center",
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000",
  },
  totalMoney: {
    marginTop: 8,
    fontSize: 32,
    fontWeight: "bold",
    color: "#000",
  },

  /** Record Title */
  recordTitle: {
    marginTop: 32,
    marginLeft: 4,
    fontSize: 20,
    fontWeight: "600",
    color: "#000",
  },

  /** Record List */
  recordList: {
    marginTop: 12,
  },

  recordItem: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,

    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",

    // shadow
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  recordLeft: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },
  recordTime: {
    fontSize: 14,
    fontWeight: "400",
    color: "#555",
  },
  recordPrice: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },
});
