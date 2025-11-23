import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function W_ShiftListScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무 교환 요청 조회</Text>
      </View>

      {/* Card 1 */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>요청자: 김민수</Text>
        <Text style={styles.cardText}>요청 날짜: 2024-10-28</Text>
        <Text style={styles.cardText}>요청 근무: 12:00–14:00</Text>
        <Text style={styles.cardText}>사유: 개인 사정</Text>
      </View>

      {/* Card 2 */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>요청자: 박지훈</Text>
        <Text style={styles.cardText}>요청 날짜: 2024-10-29</Text>
        <Text style={styles.cardText}>요청 근무: 18:00–22:00</Text>
        <Text style={styles.cardText}>사유: 일정 겹침</Text>
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5", // zinc-100
    paddingHorizontal: 24,      // px-6
    paddingTop: 64,             // pt-16
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32,           // mb-8
  },

  headerTitle: {
    fontSize: 20,               // text-xl
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,             // ml-4
  },

  card: {
    backgroundColor: "#fff",
    borderRadius: 20,           // rounded-2xl
    padding: 20,                // p-5
    marginBottom: 24,           // mb-6
    // shadow
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 4,
  },

  cardTitle: {
    fontSize: 18,               // text-lg
    fontWeight: "600",
    color: "#000",
  },

  cardText: {
    fontSize: 16,               // text-base
    color: "#374151",           // gray-700
    marginTop: 4,               // mt-1
  },
});
