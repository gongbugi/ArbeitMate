import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Bell, UserCircle, ChevronRight } from "lucide-react-native";

export default function W_HomeScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>근무지 1</Text>

        <View style={styles.icons} >
          <TouchableOpacity onPress={() => navigation.navigate("W_InformationScreen")}>
            <UserCircle size={32} color="#000" />
          </TouchableOpacity>

          <TouchableOpacity >
            <Bell size={28} color="#000" />
          </TouchableOpacity>
        </View>
      </View>

      {/* 공지사항 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_NoticeScreen")}
      >
        <Text style={styles.menuText}>공지사항</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 근무 관리 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_ScheduleManageScreen")}
      >
        <Text style={styles.menuText}>근무 관리</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 근무 가능 시간 */}
      <TouchableOpacity
        style={styles.menuCard}
        onPress={() => navigation.navigate("W_ScheduleCheckScreen")}
      >
        <Text style={styles.menuText}>근무 가능 시간</Text>
        <ChevronRight size={32} color="#999" />
      </TouchableOpacity>

      {/* 급여 관리 */}
      
      <TouchableOpacity style={styles.menuCard}
              onPress={() => navigation.navigate("W_PayScreen")}>
                <Text style={styles.menuText}>급여 관리</Text>
                <ChevronRight size={32} color="#999" />
            </TouchableOpacity>

        <View style={styles.salaryBox}>
          <Text style={styles.salaryLabel}>받아야 할 돈</Text>
          <Text style={styles.salaryValue}>50,000 원</Text>
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
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32,
  },
  title: {
    fontSize: 32,
    fontWeight: "bold",
    color: "#000",
  },
  icons: {
    flexDirection: "row",
    gap: 16,
  },

  /* Menu Cards */
  menuCard: {
    backgroundColor: "#fff",
    padding: 20,
    borderRadius: 24,
    marginBottom: 16,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 3,
  },
  menuText: {
    fontSize: 22,
    color: "#000",
  },

  /* Salary Card */
  salaryCard: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    elevation: 3,
    marginTop: 16,
    marginBottom: 40,
  },
  salaryHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,
  },
  salaryTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#000",
  },
  salaryBox: {
    backgroundColor: "#bfdbfe",
    padding: 16,
    borderRadius: 16,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  salaryLabel: {
    fontSize: 16,
    color: "#000",
  },
  salaryValue: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#1d4ed8",
  },
});
