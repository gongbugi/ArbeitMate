import React, { useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  StyleSheet,
} from "react-native";
import { X } from "lucide-react-native";
import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";
const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function SwapDetailModal({
  visible,
  onClose,
  swapData,
}) {
  const [confirmVisible, setConfirmVisible] = useState(false);

  if (!swapData) return null;

  const {
    id: requestId,
    requesterName,
    targetName,
    fromScheduleInfo,
    startTime,
    endTime,
    workDate,
    roleName,
  } = swapData;

  /** ✔ 고용주가 승인 누르면 API 호출 */
  const approveSwap = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      await axios.post(
        `${BASE_URL}/companies/${companyId}/swaps/${requestId}/approve`
      );

      alert("교환 요청을 승인했습니다.");
      setConfirmVisible(false);
      onClose();
    } catch (e) {
      console.log(e);
      alert("승인 실패");
    }
  };

  return (
    <>
      {/* === 최초 상세 화면 === */}
      <Modal transparent visible={visible} animationType="fade">
        <View style={styles.overlay}>
          <View style={styles.modal}>
            <TouchableOpacity style={styles.closeBtn} onPress={onClose}>
              <X size={22} />
            </TouchableOpacity>

            <Text style={styles.title}>
              {new Date(workDate).getMonth() + 1}월{" "}
              {new Date(workDate).getDate()}일
            </Text>

            <View style={{ marginTop: 10 }}>
              <View style={styles.row}>
                <Text style={styles.label}>교환 요청:</Text>
                <Text style={styles.value}>{requesterName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>교환 수락:</Text>
                <Text style={styles.value}>{targetName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>담당 업무:</Text>
                <Text style={styles.value}>{roleName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>근무 시간:</Text>
                <Text style={styles.value}>
                  {startTime?.slice(0, 5)} - {endTime?.slice(0, 5)}
                </Text>
              </View>
            </View>

            <TouchableOpacity
              style={styles.acceptBtn}
              onPress={() => setConfirmVisible(true)}
            >
              <Text style={styles.acceptText}>교환 요청 수락</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* === 승인 확인 모달 === */}
      <Modal transparent visible={confirmVisible} animationType="fade">
        <View style={styles.overlay}>
          <View style={styles.modal}>
            <TouchableOpacity
              style={styles.closeBtn}
              onPress={() => setConfirmVisible(false)}
            >
              <X size={22} />
            </TouchableOpacity>

            <Text style={styles.title}>교환 요청을 수락하시겠습니까?</Text>

            <View style={{ marginTop: 10 }}>
              <View style={styles.row}>
                <Text style={styles.label}>교환 요청:</Text>
                <Text style={styles.value}>{requesterName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>교환 수락:</Text>
                <Text style={styles.value}>{targetName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>담당 업무:</Text>
                <Text style={styles.value}>{roleName}</Text>
              </View>

              <View style={styles.row}>
                <Text style={styles.label}>근무 시간:</Text>
                <Text style={styles.value}>
                  {startTime?.slice(0, 5)} - {endTime?.slice(0, 5)}
                </Text>
              </View>
            </View>

            <View style={styles.confirmRow}>
              <TouchableOpacity style={styles.confirmBtn} onPress={approveSwap}>
                <Text style={styles.confirmText}>확인</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.cancelBtn}
                onPress={() => setConfirmVisible(false)}
              >
                <Text style={styles.cancelText}>취소</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </>
  );
}


const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.4)",
    justifyContent: "center",
    alignItems: "center",
  },
  modal: {
    width: "80%",
    backgroundColor: "white",
    borderRadius: 20,
    padding: 20,
  },
  closeBtn: { alignSelf: "flex-end" },

  title: {
    fontSize: 20,
    fontWeight: "bold",
    marginBottom: 12,
  },

  row: {
    flexDirection: "row",
    marginBottom: 6,
  },
  label: { fontWeight: "600", width: 90 },
  value: { fontSize: 15 },

  acceptBtn: {
    backgroundColor: "black",
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: "center",
    marginTop: 18,
  },
  acceptText: {
    color: "white",
    fontWeight: "700",
  },

  confirmRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 20,
  },

  confirmBtn: {
    backgroundColor: "#4f46e5",
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 10,
  },
  confirmText: { color: "white", fontWeight: "600" },

  cancelBtn: {
    backgroundColor: "black",
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 10,
  },
  cancelText: { color: "white", fontWeight: "600" },
});
