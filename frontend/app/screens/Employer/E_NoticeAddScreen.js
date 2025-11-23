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

export default function E_NoticeAddScreen({ navigation }) {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>공지사항 작성</Text>
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
            textAlignVertical="top"
            multiline
            numberOfLines={10}
            style={[styles.input, styles.contentInput]}
          />
        </View>

      </ScrollView>

      {/* 작성 버튼 */}
      <TouchableOpacity style={styles.submitBtn}>
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

  // Header
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 32,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  scroll: { flex: 1 },

  // Input groups
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

  // Submit button
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
