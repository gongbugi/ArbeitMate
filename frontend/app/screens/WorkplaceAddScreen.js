import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function WorkplaceAddScreen({ navigation , setRole }) {
  const handleCreate = () => {
    // 근무지 생성 로직 (DB 저장 등)
    setRole("employer");    // 역할 확정
  };
  const [storeName, setStoreName] = useState("");
  const [address, setAddress] = useState("");
  const [category, setCategory] = useState("");
  const [phone, setPhone] = useState("");

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무지 등록</Text>
      </View>

      <ScrollView showsVerticalScrollIndicator={false} style={styles.scroll}>

        {/* 매장명 */}
        <View style={styles.inputBlock}>
          <Text style={styles.label}>매장명</Text>
          <TextInput
            value={storeName}
            onChangeText={setStoreName}
            placeholder="매장명 입력"
            style={styles.input}
            placeholderTextColor="#9CA3AF"
          />
        </View>

        {/* 주소 */}
        <View style={styles.inputBlock}>
          <Text style={styles.label}>주소</Text>
          <TextInput
            value={address}
            onChangeText={setAddress}
            placeholder="주소 입력"
            style={styles.input}
            placeholderTextColor="#9CA3AF"
          />
        </View>

        {/* 업종 */}
        <View style={styles.inputBlock}>
          <Text style={styles.label}>업종</Text>
          <TextInput
            value={category}
            onChangeText={setCategory}
            placeholder="업종 입력"
            style={styles.input}
            placeholderTextColor="#9CA3AF"
          />
        </View>

        {/* 전화번호 */}
        <View style={styles.inputBlock}>
          <Text style={styles.label}>전화번호</Text>
          <TextInput
            value={phone}
            onChangeText={setPhone}
            placeholder="대표 전화번호 입력"
            keyboardType="phone-pad"
            style={styles.input}
            placeholderTextColor="#9CA3AF"
          />
        </View>

      </ScrollView>

      {/* 등록 버튼 */}
      <TouchableOpacity style={styles.submitButton}
      onPress={() => navigation.navigate("WorkplaceSelectScreen")}>
        <Text style={styles.submitText}>등록</Text>
      </TouchableOpacity>

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

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32, // mb-8
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  scroll: {
    flex: 1,
  },

  inputBlock: {
    marginBottom: 24, // mb-6
  },

  label: {
    fontSize: 20,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
  },

  input: {
    backgroundColor: "#fff",
    borderRadius: 24, // rounded-3xl
    paddingHorizontal: 20, // px-5
    paddingVertical: 12, // py-3
    fontSize: 18,
    color: "#000",
    // shadow
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },

  submitButton: {
    backgroundColor: "#000",
    borderRadius: 24,
    paddingVertical: 16,
    alignItems: "center",
    marginBottom: 16,
  },

  submitText: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#fff",
  },
});
