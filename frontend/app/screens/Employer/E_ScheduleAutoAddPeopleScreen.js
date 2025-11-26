import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import AddPeopleModal from "./AddPeopleModal";

export default function E_ScheduleAutoAddPeopleScreen({ navigation, route }) {
  const { day, period } = route.params;
  const [visible, setVisible] = useState(false);

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

      <View style={styles.card}>
        <View style={styles.row}>
          <TouchableOpacity
            style={styles.plusCircle}
            onPress={() => setVisible(true)}>
            <Text style={styles.plus}>+</Text>
          </TouchableOpacity>
          <Text style={styles.cardTitle}>인원 추가</Text>
          <AddPeopleModal visible={visible} onClose={() => setVisible(false)} />
        </View>
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

  row: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
  },

  plusCircle: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: "#000",
    justifyContent: "center",
    alignItems: "center",
    marginRight: 12,
  },

  plus: {
    color: "#fff",
    fontSize: 28,
    fontWeight: "bold",
    lineHeight: 32,
  },

  cardTitle: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000",
  },
});
