import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  ActivityIndicator,
} from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";

export default function E_WorkerManageScreen({ navigation }) {
  const [workers, setWorkers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRoles();
    loadWorkers();
  }, []);

  /** 역할(role) 목록 불러오기 */
  const loadRoles = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      const res = await client.get(`/companies/${companyId}/roles`);
      setRoles(res.data || []);
    } catch (err) {
      console.log("역할 목록 로딩 실패:", err);
    }
  };

  /**  근무자 목록 불러오기 */
  const loadWorkers = async () => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      const res = await client.get(`/companies/${companyId}/workers`);
      const workerList = res.data || [];

      setWorkers(workerList);
    } catch (err) {
      console.log("근무자 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  };

  /**  역할ID → 역할명 변환 */
  const getRoleName = (roleId) => {
    return roles.find((r) => r.roleId === roleId)?.name || "미배정";
  };

  /**  리스트 아이템 */
  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.workerCard}
      onPress={() =>
        navigation.navigate("E_WorkerUpdateScreen", { workerId: item.workerId })
      }
    >
      <View>
        <Text style={styles.workerName}>{item.name}</Text>
        <Text style={styles.workerRole}>{getRoleName(item.roleId)}</Text>
      </View>

      <ChevronRight size={22} color="#999" />
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      {/* HEADER */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무자 관리</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* LIST */}
      {loading ? (
        <ActivityIndicator
          size="large"
          color="#000"
          style={{ marginTop: 40 }}
        />
      ) : (
        <FlatList
          data={workers}
          keyExtractor={(item) => item.workerId}
          renderItem={renderItem}
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <View style={{ alignItems: "center", marginTop: 40 }}>
              <Text style={{ color: "#999" }}>등록된 근무자가 없습니다.</Text>
            </View>
          }
        />
      )}
    </View>
  );
}

/* ========== STYLES ========== */
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingTop: 64,
    paddingHorizontal: 20,
  },

  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 28,
    justifyContent: "space-between",
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },

  workerCard: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#fff",
    paddingVertical: 18,
    paddingHorizontal: 20,
    borderRadius: 20,
    marginBottom: 14,
    elevation: 2,
  },

  workerName: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
  },

  workerRole: {
    marginTop: 2,
    fontSize: 14,
    color: "#6b7280",
  },
});
