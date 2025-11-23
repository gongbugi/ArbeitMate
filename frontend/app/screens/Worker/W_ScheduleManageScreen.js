import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function W_ScheduleManageScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무 관리</Text>
      </View>

      {/* 메뉴 박스 1 */}
      <TouchableOpacity style={styles.card}>
        <Text style={styles.cardTextLarge}
        onPress={() => navigation.navigate("ScheduleScreen")}>근무표 조회</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 메뉴 박스 2 */}
      <TouchableOpacity style={styles.card}
      onPress={() => navigation.navigate("W_ShiftListScreen")}>
        <Text style={styles.cardText}>근무 교환 요청 조회</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",   // gray-100
    paddingHorizontal: 24,        // px-6
    paddingTop: 64,               // pt-16
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 40,             // mb-10
  },
  headerTitle: {
    fontSize: 20,                 // text-xl
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,               // ml-4
  },

  card: {
    backgroundColor: "#fff",
    borderRadius: 24,             // rounded-3xl
    padding: 20,                  // p-5
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,             // mb-6
    
    // shadow (iOS + Android)
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },
  cardTextLarge: {
    fontSize: 24,                 // text-2xl
    color: "#000",
    fontWeight: "600",
  },
  cardText: {
    fontSize: 20,                 // text-xl
    color: "#000",
    fontWeight: "500",
  },
});
