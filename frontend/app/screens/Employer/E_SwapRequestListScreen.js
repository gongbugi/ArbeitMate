import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  FlatList
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import axios from "axios";
import SwapDetailModal from "./SwapDetailModal"; 

const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function E_SwapRequestListScreen({ navigation }) {
  const [loading, setLoading] = useState(true);
  const [swapList, setSwapList] = useState([]);
  const [selectedSwap, setSelectedSwap] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);

  /** 📌 근무 교환 요청 가져오기 */
  const loadSwaps = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      if (!companyId) return;

      const res = await axios.get(`${BASE_URL}/companies/${companyId}/swaps`);
      setSwapList(res.data || []);
    } catch (err) {
      console.log("ERROR loading swaps:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSwaps();
  }, []);

  /**  요청 한 개 렌더 */
  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.requestCard}
      onPress={() => {
        setSelectedSwap(item);
        setModalVisible(true);
      }}
    >
      <View>
        <Text style={styles.cardTitle}>{item.fromScheduleInfo}</Text>
        <Text style={styles.cardSmall}>요청자: {item.requesterName}</Text>
        <Text style={styles.cardSmall}>대상자: {item.targetName}</Text>
      </View>
      <Text style={styles.arrow}>›</Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무 교환 요청 조회</Text>
      </View>

      {/* 내용 */}
      {loading ? (
        <ActivityIndicator size="large" color="#000" />
      ) : swapList.length === 0 ? (
        <View style={styles.box}>
          <Text style={styles.boxTitle}>근무 교환 요청</Text>
          <Text style={styles.boxContent}>교환 요청이 없습니다.</Text>
        </View>
      ) : (
        <FlatList
          data={swapList}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
        />
      )}

      {/* 상세 모달 */}
      <SwapDetailModal
        visible={modalVisible}
        onClose={() => setModalVisible(false)}
        swapData={selectedSwap}
      />

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
    marginBottom: 24,
  },

  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /* 요청 리스트 카드 */
  requestCard: {
    backgroundColor: "#fff",
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderRadius: 16,
    marginBottom: 12,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    elevation: 2,
  },

  cardTitle: {
    fontSize: 16,
    fontWeight: "bold",
    marginBottom: 6,
  },

  cardSmall: {
    fontSize: 13,
    color: "#6b7280",
  },

  arrow: {
    fontSize: 22,
    color: "#9ca3af",
  },

  /* 빈 박스 */
  box: {
    backgroundColor: "#fff",
    borderRadius: 24,
    paddingVertical: 24,
    paddingHorizontal: 24,
  },
  boxTitle: {
    fontSize: 20,
    fontWeight: "bold",
    marginBottom: 4,
  },
  boxContent: {
    fontSize: 16,
    color: "#6b7280",
    marginTop: 8,
  },
});
