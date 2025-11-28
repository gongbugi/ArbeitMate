import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  FlatList,
  ActivityIndicator,
  StyleSheet,
} from "react-native";

import { ArrowLeft, Bell, PlusCircle, } from "lucide-react-native";
import { useFocusEffect } from "@react-navigation/native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import client from "../../services/api";



export default function E_NoticeScreen({ navigation }) {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);

  useFocusEffect(
    React.useCallback(() => {
      loadNotices();
    }, [])
  );



  useEffect(() => {
    loadNotices();
  }, []);

  const loadNotices = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");

      const res = await client.get(`/companies/${companyId}/notices`);
      setNotices(res.data);
    } catch (err) {
      console.error("공지사항 불러오기 실패:", err);
    } finally {
      setLoading(false);
    }
  };



=======
  

  const formatDate = (isoString) => {
    if (!isoString) return "";
    const date = new Date(isoString);
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, "0");
    const dd = String(date.getDate()).padStart(2, "0");
    return `${yyyy}.${mm}.${dd}`;
  };

  const renderItem = ({ item }) => (
    <TouchableOpacity
      style={styles.noticeCard}
      onPress={() =>
        navigation.navigate("E_NoticeDetailScreen", {
          noticeId: item.id,
          companyId: item.companyId,
        })
      }
    >
      <View style={styles.noticeHeader}>
        <Text style={styles.noticeTitle}>{item.title}</Text>
        <Text style={styles.noticeDate}>{formatDate(item.createdAt)}</Text>
      </View>
      <Text style={styles.noticeContent} numberOfLines={2}>
        {item.content}
      </Text>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerLeft}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <ArrowLeft size={32} color="#000" />
          </TouchableOpacity>
          <Text style={styles.headerTitle}>공지사항</Text>
        </View>

        {/* 사장만 작성 가능 */}
        <TouchableOpacity onPress={() => navigation.navigate("E_NoticeAddScreen")}>
          <PlusCircle size={28} color="#000" />
        </TouchableOpacity>
      </View>

      {/* Loading */}
      {loading ? (
        <ActivityIndicator size="large" color="#000" style={{ marginTop: 20 }} />
      ) : (
        <FlatList
          data={notices}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderItem}
          showsVerticalScrollIndicator={false}
          ListEmptyComponent={
            <View style={styles.emptyBox}>
              <Text style={styles.emptyText}>등록된 공지사항이 없습니다.</Text>
            </View>
          }
        />
      )}

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
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 20,
  },
  headerLeft: {
    flexDirection: "row",
    alignItems: "center",
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    marginLeft: 16,
  },

  // Notice Card
  noticeCard: {
    backgroundColor: "#fff",
    padding: 20,
    borderRadius: 24,
    marginBottom: 16,
    elevation: 3,
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
  },
  noticeHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  noticeTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
    flex: 1,
  },
  noticeDate: {
    fontSize: 12,
    color: "#888",
    marginLeft: 8,
  },
  noticeContent: {
    fontSize: 15,
    color: "#333",
  },

  // Empty State
  emptyBox: {
    marginTop: 40,
    alignItems: "center",
  },
  emptyText: {
    color: "#999",
    fontSize: 16,
  },
});
