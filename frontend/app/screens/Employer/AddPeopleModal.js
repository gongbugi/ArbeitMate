import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  FlatList,
  StyleSheet,
  ActivityIndicator,
} from "react-native";
import { X, Plus, Minus, Check } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function AddPeopleModal({ visible, onClose, onSave }) {
  const [roles, setRoles] = useState([]);
  const [selectedRoleId, setSelectedRoleId] = useState(null);

  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [count, setCount] = useState(1);

  const [loading, setLoading] = useState(false);

  // 시간 선택 모달
  const [timeModalVisible, setTimeModalVisible] = useState(false);
  const [selectingType, setSelectingType] = useState("start"); // "start" or "end"

  const timeList = Array.from({ length: 16 }, (_, i) => {
    const hour = i + 7; // 07~22
    return `${String(hour).padStart(2, "0")}:00`;
  });

  useEffect(() => {
    if (visible) {
      loadRoles();
    }
  }, [visible]);

  const loadRoles = async () => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      const res = await client.get(`/companies/${companyId}/roles`);
      setRoles(res.data || []);
      if (res.data?.length && !selectedRoleId) {
        setSelectedRoleId(res.data[0].roleId);
      }
    } catch (err) {
      console.log("역할 목록 조회 실패:", err?.response ?? err);
    } finally {
      setLoading(false);
    }
  };

  const resetState = () => {
    setStartTime("");
    setEndTime("");
    setCount(1);
  };

  const handleSave = () => {
    if (!selectedRoleId) return alert("담당 업무를 선택하세요.");
    if (!startTime || !endTime) return alert("업무 시간을 선택해주세요.");
    if (count <= 0) return alert("필요 인원은 1명 이상이여야 합니다.");

    const roleName =
      roles.find((r) => r.roleId === selectedRoleId)?.name || "역할";

    onSave({
      roleId: selectedRoleId,
      roleName,
      startTime,
      endTime,
      requiredHeadCount: count,
    });

    resetState();
    onClose();
  };

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.modal}>
          {/* Header */}
          <View style={styles.modalHeader}>
            <TouchableOpacity onPress={onClose}>
              <X size={20} color="#000" />
            </TouchableOpacity>
            <Text style={styles.modalTitle}>인원 추가</Text>
            <View style={{ width: 20 }} />
          </View>

          {loading ? (
            <ActivityIndicator style={{ marginTop: 20 }} />
          ) : (
            <>
              {/* 역할 선택 */}
              <Text style={styles.label}>담당 업무</Text>

              <FlatList
                data={roles}
                horizontal
                keyExtractor={(item) => item.roleId}
                renderItem={({ item }) => {
                  const selected = item.roleId === selectedRoleId;
                  return (
                    <TouchableOpacity
                      style={[
                        styles.roleChip,
                        selected && styles.roleChipSelected,
                      ]}
                      onPress={() => setSelectedRoleId(item.roleId)}
                    >
                      {selected && (
                        <Check size={14} color="#fff" style={{ marginRight: 4 }} />
                      )}
                      <Text
                        style={[
                          styles.roleChipText,
                          selected && styles.roleChipTextSelected,
                        ]}
                      >
                        {item.name}
                      </Text>
                    </TouchableOpacity>
                  );
                }}
              />

              {/* 업무 시간 */}
              <Text style={[styles.label, { marginTop: 16 }]}>업무 시간</Text>

              <View style={styles.timeRow}>
                <TouchableOpacity
                  style={styles.timeSelectBox}
                  onPress={() => {
                    setSelectingType("start");
                    setTimeModalVisible(true);
                  }}
                >
                  <Text style={styles.timeText}>
                    {startTime || "시작"}
                  </Text>
                </TouchableOpacity>

                <Text style={{ marginHorizontal: 8 }}>~</Text>

                <TouchableOpacity
                  style={styles.timeSelectBox}
                  onPress={() => {
                    setSelectingType("end");
                    setTimeModalVisible(true);
                  }}
                >
                  <Text style={styles.timeText}>
                    {endTime || "종료"}
                  </Text>
                </TouchableOpacity>
              </View>

              {/* 필요 인원 */}
              <Text style={[styles.label, { marginTop: 16 }]}>필요 인원</Text>
              <View style={styles.counterRow}>
                <TouchableOpacity
                  style={styles.counterBtn}
                  onPress={() => setCount((c) => Math.max(0, c - 1))}
                >
                  <Minus size={18} />
                </TouchableOpacity>
                <Text style={styles.countText}>{count}</Text>
                <TouchableOpacity
                  style={styles.counterBtn}
                  onPress={() => setCount((c) => c + 1)}
                >
                  <Plus size={18} />
                </TouchableOpacity>
              </View>

              {/* 확인 버튼 */}
              <TouchableOpacity style={styles.saveBtn} onPress={handleSave}>
                <Text style={styles.saveText}>확인</Text>
              </TouchableOpacity>
            </>
          )}
        </View>

        {/* 시간 선택 모달 */}
        <Modal transparent visible={timeModalVisible} animationType="fade">
          <View style={styles.timeOverlay}>
            <View style={styles.timeModal}>
              <Text style={styles.timeModalTitle}>시간 선택</Text>

              <FlatList
                data={timeList}
                keyExtractor={(item) => item}
                renderItem={({ item }) => (
                  <TouchableOpacity
                    style={styles.timeItem}
                    onPress={() => {
                      if (selectingType === "start") setStartTime(item);
                      else setEndTime(item);
                      setTimeModalVisible(false);
                    }}
                  >
                    <Text style={styles.timeItemText}>{item}</Text>
                  </TouchableOpacity>
                )}
              />
            </View>
          </View>
        </Modal>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.4)",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 16,
  },
  modal: {
    width: "100%",
    backgroundColor: "#fff",
    borderRadius: 20,
    padding: 20,
  },
  modalHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: "bold",
  },
  label: {
    marginTop: 12,
    marginBottom: 4,
    color: "#555",
    fontSize: 13,
  },
  roleChip: {
    borderWidth: 1,
    borderColor: "#ddd",
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginRight: 6,
    flexDirection: "row",
    alignItems: "center",
  },
  roleChipSelected: {
    backgroundColor: "#4A67FF",
    borderColor: "#4A67FF",
  },
  roleChipText: { fontSize: 13 },
  roleChipTextSelected: { color: "#fff", fontWeight: "600" },
  timeRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  timeSelectBox: {
    flex: 1,
    padding: 12,
    borderRadius: 10,
    backgroundColor: "#f3f4f6",
    alignItems: "center",
  },
  timeText: {
    fontSize: 14,
  },
  counterRow: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 4,
  },
  counterBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: "#eee",
    justifyContent: "center",
    alignItems: "center",
  },
  countText: {
    width: 40,
    textAlign: "center",
    fontSize: 16,
    fontWeight: "600",
  },
  saveBtn: {
    marginTop: 24,
    borderRadius: 20,
    backgroundColor: "#000",
    paddingVertical: 12,
  },
  saveText: {
    color: "#fff",
    textAlign: "center",
    fontWeight: "600",
  },

  /* 시간 모달 */
  timeOverlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.45)",
    justifyContent: "center",
    alignItems: "center",
  },
  timeModal: {
    width: "70%",
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 16,
    maxHeight: "60%",
  },
  timeModalTitle: {
    fontSize: 15,
    fontWeight: "600",
    marginBottom: 12,
  },
  timeItem: {
    paddingVertical: 10,
  },
  timeItemText: {
    fontSize: 16,
  },
});
