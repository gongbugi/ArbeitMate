import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function W_ScheduleCheckScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>

          <Text style={styles.headerTitle}>근무 가능 시간</Text>
        </View>

        <TouchableOpacity>
          <Text style={styles.addButton}>추가</Text>
        </TouchableOpacity>
      </View>

      {/* List */}
      <ScrollView style={{ flex: 1 }}>

        {/* 1 */}
        <View style={styles.timeBox}>
          <Text style={styles.timeText}>12:00 - 14:00</Text>
          <Text style={styles.dayText}>월</Text>

          <TouchableOpacity style={styles.chevronBox}>
            <ChevronRight size={30} color="#000" />
          </TouchableOpacity>
        </View>

        {/* 2 */}
        <View style={styles.timeBox}>
          <Text style={styles.timeText}>12:00 - 14:00</Text>
          <Text style={styles.dayText}>화</Text>

          <TouchableOpacity style={styles.chevronBox}>
            <ChevronRight size={30} color="#000" />
          </TouchableOpacity>
        </View>

        {/* 요청 섹션 */}
        <Text style={styles.requestTitle}>요청</Text>

        <View style={styles.requestBox}>
          <View style={styles.requestRow}>
            <Text style={styles.requestDate}>10.18 (토) - 10.24 (금)</Text>
            <TouchableOpacity>
              <ChevronRight size={30} color="#000" />
            </TouchableOpacity>
          </View>
        </View>

      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5", // gray-100
    paddingHorizontal: 24, // px-6
    paddingTop: 64, // pt-16
  },

  /** HEADER */
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },
  addButton: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#000",
  },

  /** TIME BOX */
  timeBox: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    marginBottom: 16,
    position: "relative",

    // shadow
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },
  timeText: {
    fontSize: 28,
    fontWeight: "500",
    color: "#000",
  },
  dayText: {
    fontSize: 20,
    color: "rgba(0,0,0,0.6)",
    marginTop: 4,
  },
  chevronBox: {
    position: "absolute",
    right: 24,
    top: 24,
  },

  /** REQUEST SECTION */
  requestTitle: {
    fontSize: 20,
    fontWeight: "600",
    color: "#000",
    marginTop: 24,
    marginBottom: 8,
  },
  requestBox: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,

    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },
  requestRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  requestDate: {
    fontSize: 18,
    fontWeight: "700",
    color: "#000",
  },
});
