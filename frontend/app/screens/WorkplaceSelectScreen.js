import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";

export default function WorkplaceSelectScreen({ navigation }) {
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
