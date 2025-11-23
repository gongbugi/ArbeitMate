import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function W_ScheduleRequestScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>

          <Text style={styles.headerTitle}>
            10.18 (토) - 10.24 (금)
          </Text>
        </View>

        <TouchableOpacity>
          <Text style={styles.addText}>추가</Text>
        </TouchableOpacity>
      </View>

      {/* White Box */}
      <View style={styles.whiteBox}>

        {/* 항목 1 */}
        <View style={styles.itemRow}>
          <View>
            <Text style={styles.timeText}>12:00 - 14:00</Text>
            <Text style={styles.dayText}>월</Text>
          </View>

          <TouchableOpacity>
            <ChevronRight size={28} color="#000" />
          </TouchableOpacity>
        </View>

        {/* 항목 2 */}
        <View style={styles.itemRow}>
          <View>
            <Text style={styles.timeText}>12:00 - 14:00</Text>
            <Text style={styles.dayText}>화</Text>
          </View>

          <TouchableOpacity>
            <ChevronRight size={28} color="#000" />
          </TouchableOpacity>
        </View>

      </View>

      {/* 저장 버튼 */}
      <TouchableOpacity style={styles.saveButton}>
        <Text style={styles.saveText}>저장</Text>
      </TouchableOpacity>

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

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,           // mb-6
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 18,               // text-lg
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,             // ml-4
  },
  addText: {
    fontSize: 16,               // text-base
    fontWeight: "bold",
    color: "#000",
  },

  whiteBox: {
    backgroundColor: "#fff",
    borderRadius: 24,           // rounded-3xl
    padding: 24,                // p-6
    flex: 1,
    // shadow
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },

  itemRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,           // mb-6
  },

  timeText: {
    fontSize: 28,               // text-3xl
    fontWeight: "600",
    color: "#000",
  },
  dayText: {
    fontSize: 20,               // text-xl
    color: "rgba(0,0,0,0.6)",
    marginTop: 4,               // mt-1
  },

  saveButton: {
    backgroundColor: "#000",
    borderRadius: 24,           // rounded-3xl
    paddingVertical: 16,        // py-4
    alignItems: "center",
    marginTop: 24,              // mt-6
  },
  saveText: {
    color: "#fff",
    fontSize: 24,               // text-2xl
    fontWeight: "bold",
  },
});
