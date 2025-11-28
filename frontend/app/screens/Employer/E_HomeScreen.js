import React from "react";
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from "react-native";
import { ChevronRight, Bell, UserCircle, Home } from "lucide-react-native";

export default function E_HomeScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>근무지 1</Text>

        <View style={styles.headerIcons}>
          <TouchableOpacity onPress={() => navigation.navigate("E_InformationScreen")}>
            <Home size={32} color="#000" />
          </TouchableOpacity>

          <TouchableOpacity>
            <Bell size={32} color="#000" />
          </TouchableOpacity>
        </View>
      </View>

      {/* Scroll */}
      <ScrollView showsVerticalScrollIndicator={false}>

        {/* 공지사항 */}
        <TouchableOpacity style={styles.menuCard}
        onPress={() => navigation.navigate("E_NoticeScreen")}>
          <Text style={styles.menuText}>공지사항</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 근무 관리 */}
        <TouchableOpacity style={styles.menuCard}
        onPress={() => navigation.navigate("E_ScheduleManageScreen")}>
          <Text style={styles.menuText}>근무 관리</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 급여 관리 */}
        <TouchableOpacity style={styles.menuCard}
        onPress={() => navigation.navigate("E_PayListScreen")}>
          <Text style={styles.menuText}>급여 관리</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 지급해야 할 돈 박스 */}
        <View style={styles.moneyBox}>
          <View style={styles.moneyRow}>
            <Text style={styles.moneyLabel}>지급해야 할 돈</Text>
            <Text style={styles.moneyValue}>- 50,000 원</Text>
          </View>
        </View>

        {/* 근무자 */}
        <TouchableOpacity style={styles.menuCard}
        onPress={() => navigation.navigate("E_WorkerManageScreen")}>
          <Text style={styles.menuText}>근무자</Text>
          <ChevronRight size={32} color="#999" />
        </TouchableOpacity>

        {/* 인원 정보 */}
        <View style={styles.infoCard}>
          <View style={styles.infoRow}>

            <View style={styles.infoItem}>
              <Text style={styles.infoNumber}>2</Text>
              <Text style={styles.infoLabel}>현재 인원</Text>
            </View>

            <View style={styles.infoItem}>
              <Text style={styles.infoNumber}>1</Text>
              <Text style={styles.infoLabel}>승인 대기</Text>
            </View>

          </View>
        </View>

      </ScrollView>
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

  // Header
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000",
  },
  headerIcons: {
    flexDirection: "row",
    gap: 16,
  },

  // Menu card
  menuCard: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
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

  // Money box
  moneyBox: {
    backgroundColor: "#fecaca",
    borderRadius: 20,
    padding: 20,
    marginBottom: 16,
    elevation: 3,
  },
  moneyRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  moneyLabel: {
    fontSize: 18,
  },
  moneyValue: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#dc2626",
  },

  // Info card
  infoCard: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 24,
    elevation: 3,
    marginBottom: 40,
  },
  infoRow: {
    flexDirection: "row",
    justifyContent: "space-around",
  },
  infoItem: {
    alignItems: "center",
  },
  infoNumber: {
    fontSize: 48,
    fontWeight: "bold",
  },
  infoLabel: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: "bold",
  },
});
