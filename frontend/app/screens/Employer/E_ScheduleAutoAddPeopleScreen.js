import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ScheduleAutoAddPeopleScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무표 자동 생성</Text>
      </View>

      {/* 요일 */}
      <Text style={styles.dayLabel}>{day}</Text>

      {/* Card */}
      <View style={styles.card}>
        <Text style={styles.cardTitle}>인원 추가</Text>

        <TouchableOpacity
          style={styles.plusButton}
          onPress={() => navigation.navigate("E_AddPeopleModal")}
        >
          <View style={styles.plusIcon} />
        </TouchableOpacity>
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
    alignItems: "center",
    marginBottom: 24,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /* 요일 텍스트 */
  dayLabel: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginBottom: 16,
  },

  /* Card */
  card: {
    backgroundColor: "#fff",
    borderRadius: 30,
    padding: 24,
    elevation: 3,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
  },

  cardTitle: {
    fontSize: 28,
    fontWeight: "bold",
    textAlign: "center",
    color: "#000",
    marginBottom: 32,
  },

  plusButton: {
    width: 48,
    height: 48,
    backgroundColor: "rgba(0,0,0,0.05)",
    borderRadius: 12,
    justifyContent: "center",
    alignItems: "center",
  },

  plusIcon: {
    width: 32,
    height: 32,
    backgroundColor: "rgba(0,0,0,0.3)",
    borderRadius: 6,
  },
});
