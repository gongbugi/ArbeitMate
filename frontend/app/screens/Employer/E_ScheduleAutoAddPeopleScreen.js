import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
} from "react-native";
import { ArrowLeft, Trash2 } from "lucide-react-native";
import AddPeopleModal from "./AddPeopleModal";

export default function E_ScheduleAutoAddPeopleScreen({ navigation, route }) {
  const {
    weekdayIndex,
    weekdayLabel,
    patterns: initialPatterns = [],
    onSave,
  } = route.params;

  const [patterns, setPatterns] = useState(initialPatterns);
  const [modalVisible, setModalVisible] = useState(false);

  const handleAddPattern = (pattern) => {
    setPatterns((prev) => [...prev, { ...pattern, id: Date.now().toString() }]);
  };

  const handleRemove = (id) => {
    setPatterns((prev) => prev.filter((p) => p.id !== id));
  };

  // 이 화면에서 뒤로 갈 때 부모(요일 화면)에 저장
  const handleGoBack = () => {
    if (onSave) {
      onSave(weekdayIndex, patterns);
    }
    navigation.goBack();
  };

  const renderItem = ({ item }) => (
    <View style={styles.itemCard}>
      <View>
        <Text style={styles.itemTitle}>
          {item.roleName || "역할 미지정"} {item.requiredHeadCount}명
        </Text>
        <Text style={styles.itemTime}>
          {item.startTime} ~ {item.endTime}
        </Text>
      </View>
      <TouchableOpacity onPress={() => handleRemove(item.id)}>
        <Trash2 size={20} color="#9ca3af" />
      </TouchableOpacity>
    </View>
  );

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={handleGoBack}>
          <ArrowLeft size={28} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무표 자동 생성</Text>
        <View style={{ width: 28 }} />
      </View>

      <Text style={styles.dayTitle}>{weekdayLabel}요일</Text>

      {/* 패턴 리스트 */}
      <FlatList
        data={patterns}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        ListEmptyComponent={
          <View style={styles.emptyBox}>
            <Text style={styles.emptyText}>아직 추가된 근무 패턴이 없습니다.</Text>
          </View>
        }
        contentContainerStyle={{ paddingVertical: 16 }}
      />

      {/* 인원 추가 버튼 */}
      <TouchableOpacity
        style={styles.addBtn}
        onPress={() => setModalVisible(true)}
      >
        <Text style={styles.addText}>+ 인원 추가</Text>
      </TouchableOpacity>

      <AddPeopleModal
        visible={modalVisible}
        onClose={() => setModalVisible(false)}
        onSave={handleAddPattern}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f3f4f6",
    paddingTop: 64,
    paddingHorizontal: 20,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },
  dayTitle: {
    marginTop: 16,
    fontSize: 18,
    fontWeight: "bold",
  },
  itemCard: {
    marginTop: 12,
    backgroundColor: "#fff",
    borderRadius: 18,
    paddingHorizontal: 16,
    paddingVertical: 14,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  itemTitle: {
    fontSize: 16,
    fontWeight: "600",
  },
  itemTime: {
    marginTop: 4,
    fontSize: 13,
    color: "#6b7280",
  },
  emptyBox: {
    marginTop: 40,
    alignItems: "center",
  },
  emptyText: {
    color: "#9ca3af",
  },
  addBtn: {
    marginTop: 16,
    backgroundColor: "#4f46e5",
    borderRadius: 28,
    height: 54,
    alignItems: "center",
    justifyContent: "center",
  },
  addText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "bold",
  },
});