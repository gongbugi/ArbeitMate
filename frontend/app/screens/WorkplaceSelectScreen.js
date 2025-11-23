import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";

export default function WorkplaceSelectScreen({ navigation }) {

  const fetchWorkplaces = async () => {
    try {
      const res = await fetch("http://13.209.21.34:8080/api/company/my", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (!res.ok) {
        console.log("근무지 조회 실패");
        return;
      }

      const data = await res.json();
      setWorkplaces(data); // 리스트 저장

    } catch (err) {
      console.log("서버 오류:", err);
    }
  };

  // 화면 처음 로드 or 등록 후 refresh → 자동 새로고침
  useEffect(() => {
    fetchWorkplaces();
  }, [route.params?.refresh]);

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerText}>근무지</Text>
      </View>

      {/* Add Button Card */}
      <TouchableOpacity
        style={styles.card}
        onPress={() => navigation.navigate("WorkplaceRegisterScreen")}
      >
        <Ionicons name="add" size={56} color="#555" />
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F4F4F5", // 전체 앱 통일 색
    paddingTop: 80,
    alignItems: "center",
  },

  // 상단 제목
  header: {
    marginBottom: 40,
  },

  headerText: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000",
  },

  // 큰 카드 버튼
  card: {
    width: 150,
    height: 150,
    backgroundColor: "#fff",
    borderRadius: 24,
    alignItems: "center",
    justifyContent: "center",
    
    // shadow
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 6,
    elevation: 4,
  },
});
