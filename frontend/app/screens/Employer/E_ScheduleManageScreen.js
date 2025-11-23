import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function WorkManageScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />

        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무 관리</Text>
      </View>

      <ScrollView showsVerticalScrollIndicator={false}>

        {/* 근무표 조회 */}
        <TouchableOpacity style={styles.card}
        onPress={() => navigation.navigate("ScheduleScreen")}>
          <Text style={styles.cardTitle}>근무표 조회</Text>
          <ChevronRight size={28} color="#555" />
        </TouchableOpacity>

        {/* 근무표 자동 생성 */}
        <TouchableOpacity style={styles.card}
        onPress={() => navigation.navigate("E_ScheduleAutoAddScreen")}>
          <Text style={styles.cardTitle}>근무표 자동 생성</Text>
          <ChevronRight size={28} color="#555" />
        </TouchableOpacity>

        {/* 근무 교환 요청 조회 */}
        <TouchableOpacity style={styles.card}
        onPress={() => navigation.navigate("E_ShiftRequestListScreen")}>
          <Text style={styles.cardTitleSmall}>근무 교환 요청 조회</Text>
          <ChevronRight size={28} color="#555" />
        </TouchableOpacity>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6", // gray-100
    paddingHorizontal: 24,      // px-6
    paddingTop: 64,             // pt-16
  },

  // Header
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32, // mb-8
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  // Card
  card: {
    backgroundColor: "#fff",
    borderRadius: 24,      // rounded-3xl
    padding: 24,           // p-6
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,      // mb-4
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },

  cardTitle: {
    fontSize: 24,   // text-2xl
    color: "#000",
  },
  cardTitleSmall: {
    fontSize: 20,   // text-xl
    color: "#000",
  },
});
