import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function E_ShiftRequestListScreen({ navigation }) {
  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무 교환 요청 조회</Text>
      </View>

      {/* 요청 박스 */}
      <View style={styles.box}>
        <Text style={styles.boxTitle}>근무 교환 요청</Text>

        <Text style={styles.boxContent}>
          교환 요청이 없습니다.
        </Text>
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6", // gray-100
    paddingHorizontal: 24, // px-6
    paddingTop: 64, // pt-16
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 40, // mb-10
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16, // ml-4
  },

  box: {
    backgroundColor: "#fff",
    borderRadius: 24, // rounded-3xl
    paddingVertical: 24, // py-6
    paddingHorizontal: 24, // px-6
    elevation: 3,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
  },

  boxTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginBottom: 4,
  },

  boxContent: {
    fontSize: 16,
    color: "#6b7280", // gray-500
    marginTop: 8,
  },
});
