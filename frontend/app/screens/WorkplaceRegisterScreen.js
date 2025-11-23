import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";

export default function WorkplaceRegisterScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back-outline" size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerText}>근무지 등록</Text>

        {/* 가운데 정렬 유지용 빈 박스 */}
        <View style={{ width: 32 }} />
      </View>

      {/* 선택 카드 영역 */}
      <View style={styles.cardContainer}>

        {/* 근무지 생성 */}
        <TouchableOpacity style={styles.card}
        onPress={() => navigation.navigate("WorkplaceAddScreen")}>
          <Ionicons name="home-outline" size={42} color="#3B82F6" />
          <Text style={styles.cardText}>근무지 생성</Text>
        </TouchableOpacity>

        {/* 근무지 가입 */}
        <TouchableOpacity
          style={styles.card}
          onPress={() => navigation.navigate("WorkplaceJoinScreen")}
        >
          <Ionicons name="mail-outline" size={42} color="#3B82F6" />
          <Text style={styles.cardText}>근무지 가입</Text>
        </TouchableOpacity>

      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F4F4F5", // gray-100 느낌
    paddingHorizontal: 24,
    paddingTop: 64,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 50,
  },

  headerText: {
    fontSize: 22,
    fontWeight: "bold",
    color: "#000",
  },

  cardContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
    marginTop: 40,
  },

  card: {
    backgroundColor: "#fff",
    borderRadius: 24,
    width: 150,
    height: 150,
    alignItems: "center",
    justifyContent: "center",

    // shadow
    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 6,
    elevation: 3,
  },

  cardText: {
    marginTop: 12,
    fontSize: 18,
    fontWeight: "600",
    color: "#000",
  },
});
