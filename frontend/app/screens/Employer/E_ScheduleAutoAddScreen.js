import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ScheduleAutoAddScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Top Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무표 자동 생성</Text>

        {/* 오른쪽 공간 확보용 */}
        <View style={{ width: 32 }} />
      </View>

      {/* Section title */}
      <Text style={styles.sectionTitle}>보낸 요청</Text>

      {/* White Card */}
      <View style={styles.card}>
        {/* 내부 상단 작은 아이콘(임시) */}
        <TouchableOpacity style={styles.cardBackBtn}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>

        <Text style={styles.cardTitle}
        onPress={() => navigation.navigate("E_ScheduleAutoAddPeriodScreen")}>요청 작성</Text>
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F3F3F4", // Grays-Gray-6 비슷
    paddingTop: 80,
    paddingHorizontal: 20,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 20,
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },

  sectionTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: "#000",
    marginTop: 20,
    marginBottom: 10,
    marginLeft: 5,
  },

  card: {
    width: "100%",
    height: 660,
    backgroundColor: "#fff",
    borderRadius: 30,
    paddingTop: 40,
    alignItems: "center",
  },

  cardBackBtn: {
    position: "absolute",
    top: 20,
    left: 20,
  },

  cardTitle: {
    fontSize: 28,
    fontWeight: "600",
    color: "#000",
    marginTop: 10,
  },
});
