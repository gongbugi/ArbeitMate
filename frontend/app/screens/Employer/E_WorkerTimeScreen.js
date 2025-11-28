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

export default function E_WorkerTimeScreen({ navigation, route }) {
  const { workerId } = route.params;

  const [times, setTimes] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRoles();
    loadTimes();
  }, []);

  /** 📌 역할 목록 불러오기 */
  const loadRoles = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      const res = await client.get(`/companies/${companyId}/roles`);
      setRoles(res.data || []);
    } catch (err) {
      console.log("역할 목록 실패:", err);
    }
  };

  /** 📌 근무자 고정 근무시간 불러오기 */
  const loadTimes = async () => {
    try {
      setLoading(true);
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      const res = await client.get(
        `/companies/${companyId}/workers/${workerId}/fixed-times`
      );

      setTimes(res.data || []);
    } catch (err) {
      console.log("고정 근무시간 조회 실패:", err);
      setTimes([]);
    } finally {
      setLoading(false);
    }
  };

  /** 📌 역할ID → 역할명 */
  const getRoleName = (roleId) => {
    return roles.find((r) => r.roleId === roleId)?.name || "미배정";
  };

  /** 📌 요일 숫자 → 텍스트 */
  const weekdayLabel = ["월", "화", "수", "목", "금", "토", "일"];

  /** 📌 렌더 아이템 */
  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.timeCard}
      onPress={() =>
        navigation.navigate("E_WorkerTimeUpdateScreen", {
          workerId,
          fixedTimeId: item.fixedTimeId,
        })
      }
    >
      <View>
        <Text style={styles.roleText}>{getRoleName(item.roleId)}</Text>

        <Text style={styles.weekdayText}>
          요일: {weekdayLabel[item.weekday]}
        </Text>

        <Text style={styles.timeText}>
          {item.startTime.slice(0, 5)} ~ {item.endTime.slice(0, 5)}
        </Text>
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

        <Text style={styles.headerTitle}>고정 근무 시간</Text>

        <TouchableOpacity
          onPress={() =>
            navigation.navigate("E_WorkerTimeUpdateScreen", { workerId })
          }
        >
          <Text style={styles.addText}>추가</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator
          size="large"
          color="#000"
          style={{ marginTop: 40 }}
        />
      ) : (
        <FlatList
          data={times}
          keyExtractor={(item) => item.fixedTimeId.toString()}
          renderItem={renderItem}
          ListEmptyComponent={
            <View style={{ marginTop: 40, alignItems: "center" }}>
              <Text style={{ color: "#999" }}>등록된 근무시간이 없습니다.</Text>
            </View>
          }
        />
      )}
    </View>
  );
}

/* ---------- 스타일 ---------- */

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingHorizontal: 20,
    paddingTop: 64,
  },

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 24,
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
  },

  addText: {
    fontSize: 16,
    color: "#000",
    fontWeight: "600",
  },

  timeCard: {
    backgroundColor: "#fff",
    padding: 18,
    borderRadius: 20,
    marginBottom: 14,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 2,
  },

  roleText: {
    fontSize: 17,
    fontWeight: "bold",
    color: "#000",
  },

  weekdayText: {
    marginTop: 4,
    color: "#6b7280",
  },

  timeText: {
    marginTop: 4,
    fontSize: 15,
    fontWeight: "600",
    color: "#000",
  },
});
