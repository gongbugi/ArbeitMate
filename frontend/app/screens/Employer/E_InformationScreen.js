import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  TextInput,
  FlatList,
} from "react-native";
import { ArrowLeft, Plus, X } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function E_InformationScreen({ navigation }) {
  const [companyInfo, setCompanyInfo] = useState(null);
  const [roles, setRoles] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [newRole, setNewRole] = useState("");

  const loadData = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      // 회사 정보 불러오기
      const infoRes = await client.get(`/companies/${companyId}`);
      setCompanyInfo(infoRes.data);

      // 역할 목록 불러오기
      const roleRes = await client.get(`/companies/${companyId}/roles`);
      setRoles(roleRes.data || []);
    } catch (err) {
      console.log("회사 정보/역할 불러오기 실패:", err);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  /** 역할 추가 */
  const addRole = async () => {
    if (!newRole.trim()) return;

    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      await client.post(`/companies/${companyId}/roles`, {
        name: newRole.trim(),
      });

      setNewRole("");
      setModalVisible(false);

      // 최신 역할 다시 불러오기
      loadData();
    } catch (err) {
      console.log("업무 추가 실패:", err);
      alert("업무 추가에 실패했습니다.");
    }
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무지 등록</Text>
      </View>

      {companyInfo ? (
        <>
          <View style={styles.inputContainer}>

            <Text style={styles.label}>매장명</Text>
            <View style={styles.inputBox}>
              <Text style={styles.inputText}>{companyInfo.name}</Text>
            </View>

            <Text style={styles.label}>주소</Text>
            <View style={styles.inputBox}>
              <Text style={styles.inputText}>{companyInfo.address}</Text>
            </View>

            <Text style={styles.label}>초대코드</Text>
            <View style={styles.inputBox}>
              <Text style={styles.inputText}>{companyInfo.inviteCode}</Text>
            </View>
          </View>

          {/* 역할 리스트 */}
          <Text style={styles.label}>업무</Text>

          <View style={styles.roleListBox}>
            {roles.length === 0 ? (
              <Text style={{ color: "#bbb" }}>등록된 업무가 없습니다.</Text>
            ) : (
              <FlatList
                data={roles}
                keyExtractor={(item) => item.roleId}
                renderItem={({ item }) => (
                  <View style={styles.roleItem}>
                    <Text style={styles.roleText}>• {item.name}</Text>
                  </View>
                )}
              />
            )}
          </View>

          {/* 업무 추가 버튼 */}
          <TouchableOpacity
            style={styles.addRoleBtn}
            onPress={() => setModalVisible(true)}
          >
            <Plus size={18} color="#000" />
            <Text style={{ marginLeft: 6, fontWeight: "600" }}>업무 추가</Text>
          </TouchableOpacity>
        </>
      ) : (
        <Text>불러오는 중...</Text>
      )}

      {/* 완료 버튼 */}
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          alert("설정이 저장되었습니다.");
          navigation.goBack();
        }}
      >
        <Text style={styles.buttonText}>등록</Text>
      </TouchableOpacity>

      {/* 업무 추가 모달 */}
      <Modal visible={modalVisible} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalBox}>
            <TouchableOpacity
              style={styles.modalCloseBtn}
              onPress={() => setModalVisible(false)}
            >
              <X size={22} color="#000" />
            </TouchableOpacity>

            <Text style={styles.modalTitle}>업무 추가</Text>

            <TextInput
              style={styles.modalInput}
              placeholder="업무명을 입력하세요"
              value={newRole}
              onChangeText={setNewRole}
            />

            <TouchableOpacity style={styles.modalAddBtn} onPress={addRole}>
              <Text style={styles.modalAddText}>추가</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
}

/* Styles */
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
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: "bold",
    marginLeft: 16,
    color: "#000",
  },
  inputContainer: {
    marginBottom: 20,
  },
  label: {
    fontSize: 18,
    fontWeight: "600",
    color: "#000",
    marginBottom: 8,
    marginLeft: 6,
  },
  inputBox: {
    backgroundColor: "#fff",
    height: 50,
    borderRadius: 30,
    justifyContent: "center",
    paddingHorizontal: 20,
    marginBottom: 24,
  },
  inputText: {
    fontSize: 18,
    color: "#000",
  },
  roleListBox: {
    backgroundColor: "#fff",
    minHeight: 60,
    borderRadius: 20,
    padding: 16,
    marginBottom: 16,
  },
  roleItem: {
    paddingVertical: 4,
  },
  roleText: {
    fontSize: 16,
    color: "#000",
  },
  addRoleBtn: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 24,
  },
  button: {
    backgroundColor: "#000",
    paddingVertical: 16,
    borderRadius: 30,
    marginTop: 20,
  },
  buttonText: {
    textAlign: "center",
    color: "#fff",
    fontSize: 22,
    fontWeight: "bold",
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.4)",
    justifyContent: "center",
    alignItems: "center",
  },
  modalBox: {
    width: "80%",
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 20,
  },
  modalCloseBtn: {
    alignSelf: "flex-end",
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: "700",
    marginBottom: 12,
  },
  modalInput: {
    backgroundColor: "#f3f4f6",
    borderRadius: 10,
    padding: 12,
    marginBottom: 14,
    fontSize: 16,
  },
  modalAddBtn: {
    backgroundColor: "#000",
    paddingVertical: 12,
    borderRadius: 10,
  },
  modalAddText: {
    color: "#fff",
    textAlign: "center",
    fontSize: 16,
    fontWeight: "bold",
  },
});
