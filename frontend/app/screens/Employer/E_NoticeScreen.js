import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from "react-native";
import { ArrowLeft, Bell } from "lucide-react-native";

export default function E_NoticeScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>

          <Text style={styles.headerTitle}>공지사항</Text>
        </View>

        <TouchableOpacity onPress={() => navigation.navigate("E_NoticeAddScreen")}>
          <Bell size={28} color="#000" />
        </TouchableOpacity>
      </View>

      {/* 공지 리스트 */}
      <ScrollView showsVerticalScrollIndicator={false}>

        {/* 공지 1 */}
        <View style={styles.noticeCard}>
          <View style={styles.noticeHeaderRow}>
            <Text style={styles.noticeTitle}>지점 휴일 안내</Text>
            <Text style={styles.noticeDate}>2025.10.04</Text>
          </View>
          <Text style={styles.noticeContent}>
            내일은 지점 휴무입니다.
          </Text>
        </View>

        {/* 공지 2 */}
        <View style={styles.noticeCard}>
          <View style={styles.noticeHeaderRow}>
            <Text style={styles.noticeTitle}>메뉴 교육</Text>
            <Text style={styles.noticeDate}>2025.10.13</Text>
          </View>
          <Text style={styles.noticeContent}>
            이번 주 금요일에 신메뉴 교육이 있습니다.
          </Text>
        </View>

      </ScrollView>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6", // gray-100 비슷
    paddingHorizontal: 24,      // px-6
    paddingTop: 64,             // pt-16
  },

  // Header
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32, // mb-8
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16, // ml-4
  },

  // Notice card
  noticeCard: {
    backgroundColor: "#fff",
    borderRadius: 24,   // rounded-3xl
    padding: 20,        // p-5
    marginBottom: 24,   // mb-6
    elevation: 3,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  noticeHeaderRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    marginBottom: 8, // mb-2
  },
  noticeTitle: {
    fontSize: 20,
    color: "#6b7280", // text-gray-500
  },
  noticeDate: {
    fontSize: 12,
    color: "#6b7280",
  },
  noticeContent: {
    fontSize: 16,
    color: "#000",
  },
});
