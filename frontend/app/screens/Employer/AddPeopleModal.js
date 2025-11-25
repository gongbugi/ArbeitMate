import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  TextInput,
  Modal,
  StyleSheet,
} from "react-native";
import { X, Plus, Minus } from "lucide-react-native";
import axios from "axios";

export default function AddPeopleModal({ visible, onClose }) {
  const [count, setCount] = useState(0);

  return (
    <Modal visible={visible} transparent animationType="fade">

      {/* 배경 */}
      <View style={styles.dimmed}>
        
        {/* 팝업 박스 */}
        <View style={styles.modalBox}>

          {/* X 버튼 */}
          <TouchableOpacity onPress={onClose} style={styles.closeBtn}>
            <X size={26} color="#000" />
          </TouchableOpacity>

          <Text style={styles.modalTitle}>인원 추가</Text>

          {/* 담당 업무 */}
          <Text style={styles.label}>담당 업무</Text>
          <TextInput placeholder="입력" style={styles.input} />

          {/* 업무 시간 */}
          <Text style={styles.label}>업무 시간</Text>
          <TextInput placeholder="예: 12:00 - 14:00" style={styles.input} />

          {/* 필요 인원 */}
          <View style={styles.rowBetween}>
            <Text style={styles.label}>필요 인원</Text>

            <View style={styles.counterRow}>
              <TouchableOpacity
                style={styles.counterBtn}
                onPress={() => setCount(Math.max(0, count - 1))}
              >
                <Minus size={20} color="#000" />
              </TouchableOpacity>

              <Text style={styles.countText}>{count}</Text>

              <TouchableOpacity
                style={styles.counterBtn}
                onPress={() => setCount(count + 1)}
              >
                <Plus size={20} color="#000" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 확인 버튼 */}
          <TouchableOpacity style={styles.saveBtn} onPress={onClose}>
            <Text style={styles.saveText}>확인</Text>
          </TouchableOpacity>

        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  dimmed: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.5)",
    justifyContent: "center",
    alignItems: "center",
  },
  modalBox: {
    width: "80%",
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
  },
  closeBtn: {
    position: "absolute",
    top: 20,
    right: 20,
  },
  modalTitle: {
    fontSize: 22,
    fontWeight: "bold",
    textAlign: "center",
    marginBottom: 20,
  },
  label: {
    marginTop: 16,
    marginBottom: 6,
    fontWeight: "600",
  },
  input: {
    backgroundColor: "#eee",
    borderRadius: 12,
    padding: 10,
  },
  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 16,
  },
  counterRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  counterBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: "#ddd",
    justifyContent: "center",
    alignItems: "center",
  },
  countText: {
    fontSize: 20,
    fontWeight: "600",
  },
  saveBtn: {
    backgroundColor: "#000",
    padding: 16,
    borderRadius: 20,
    marginTop: 24,
  },
  saveText: {
    textAlign: "center",
    color: "#fff",
    fontWeight: "bold",
  },
});
