import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";

export default function W_InformationScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>내 정보</Text>

        {/* 오른쪽 공간 확보 */}
        <View style={{ width: 32 }} />
      </View>

      <View style={styles.sectionWrapper}>

        {/* 이름 */}
        <View style={styles.section}>
          <Text style={styles.label}>이름</Text>

          <View style={styles.box}>
            <Text style={styles.value}>김XX</Text>
          </View>
        </View>

        {/* 담당 업무 */}
        <View style={styles.section}>
          <Text style={styles.label}>담당 업무</Text>

          <View style={styles.box}>
            <Text style={styles.value}>서빙</Text>
          </View>
        </View>

        {/* 시급 */}
        <View style={styles.section}>
          <Text style={styles.label}>시급</Text>

          <View style={[styles.box, styles.rowBetween]}>
            <Text style={styles.value}>12,500</Text>
            <Text style={styles.valueBold}>원</Text>
          </View>
        </View>

        {/* 고정 근무 시간 */}
        <View style={styles.section}>
          <Text style={styles.label}>고정 근무 시간</Text>

          <View style={styles.box}>
            <Text style={styles.value}>월 12:00-14:00</Text>
          </View>
        </View>

        {/* 근무 가능 시간 */}
        <View style={styles.section}>
          <View style={styles.rowBetween}>
            <Text style={styles.label}>근무 가능 시간</Text>
            <TouchableOpacity>
              <ChevronRight size={28} color="#000" />
            </TouchableOpacity>
          </View>

          <View style={styles.boxLarge}>
            <Text style={styles.valueBlack}>월 12:00-14:00</Text>
            <Text style={styles.valueBlack}>화 12:00-14:00</Text>
          </View>
        </View>

      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",
    paddingHorizontal: 24, // px-6
    paddingTop: 64,        // pt-16
  },

  /* Header */
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 32, // mb-8
  },
  headerTitle: {
    fontSize: 20,    // text-xl
    fontWeight: "bold",
    color: "#000",
  },

  /* Section wrapper */
  sectionWrapper: {
    gap: 24, // space-y-6
  },

  /* Individual section */
  section: {},

  label: {
    fontSize: 20,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
  },

  /* White box */
  box: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 12,
    paddingHorizontal: 20,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 3,
  },

  boxLarge: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 16,
    paddingHorizontal: 20,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowRadius: 6,
    elevation: 3,
    gap: 8,
  },

  value: {
    fontSize: 20,
    color: "rgba(0,0,0,0.5)",
  },

  valueBold: {
    fontSize: 20,
    fontWeight: "700",
    color: "rgba(0,0,0,0.8)",
  },

  valueBlack: {
    fontSize: 20,
    color: "#000",
  },

  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
});
