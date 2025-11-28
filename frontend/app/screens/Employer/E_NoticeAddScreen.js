import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Alert,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api"; // Worker/E 공통 axios 인스턴스

export default function E_NoticeAddScreen({ navigation }) {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const submitNotice = async () => {
    if (!title.trim() || !content.trim()) {
      Alert.alert("알림", "제목과 내용을 모두 입력해주세요.");
      return;
    }

    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      await client.post(`/companies/${companyId}/notices`, {
        title,
        content,
      });

      Alert.alert("성공", "공지사항이 등록되었습니다.");
      navigation.goBack();
    } catch (err) {
      console.error("공지사항 등록 실패:", err);
      Alert.alert("오류", "공지사항 등록 중 문제가 발생했습니다.");
    }
  };

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>공지사항 작성</Text>

        <View style={{ width: 32 }} />
      </View>

      <ScrollView showsVerticalScrollIndicator={false} style={styles.scroll}>

        {/* 제목 */}
        <View style={styles.inputGroup}>
          <Text style={styles.label}>제목</Text>
          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="제목을 입력하세요"
            style={styles.input}
          />
        </View>

        {/* 내용 */}
        <View style={styles.inputGroup}>
          <Text style={styles.label}>내용 작성</Text>

          <TextInput
            value={content}
            onChangeText={setContent}
            placeholder="내용을 입력하세요"
            multiline
            numberOfLines={10}
            textAlignVertical="top"
            style={[styles.input, styles.contentInput]}
          />
        </View>

      </ScrollView>

      {/* 작성 버튼 */}
      <TouchableOpacity style={styles.submitBtn} onPress={submitNotice}>
        <Text style={styles.submitText}>작성</Text>
      </TouchableOpacity>

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

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32,
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  scroll: { flex: 1 },

  inputGroup: {
    marginBottom: 24,
  },
  label: {
    fontSize: 20,
    color: "#737373",
    marginBottom: 8,
  },
  input: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingHorizontal: 20,
    paddingVertical: 12,
    fontSize: 18,
    elevation: 3,
  },
  contentInput: {
    height: 350,
    paddingVertical: 16,
  },

  submitBtn: {
    backgroundColor: "#000",
    borderRadius: 24,
    paddingVertical: 16,
    alignItems: "center",
    marginBottom: 16,
  },
  submitText: {
    color: "#fff",
    fontSize: 22,
    fontWeight: "bold",
  },
});
