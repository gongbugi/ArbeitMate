import React, { useState, useCallback } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  SectionList,
  ActivityIndicator,
  RefreshControl,
  Alert
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useFocusEffect } from '@react-navigation/native';
import client from "../../services/api";

export default function W_ShiftListScreen({ navigation }) {
  const [sections, setSections] = useState([]); // 섹션 데이터
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [myMemberId, setMyMemberId] = useState(null);

  const fetchRequests = async () => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      
      // 1. 내 정보 조회 (내 ID 확인용)
      const meRes = await client.get("/members/me");
      const myId = meRes.data.id;
      setMyMemberId(myId);

      // 2. 요청 목록 조회
      const res = await client.get(`/companies/${companyId}/swaps/my`);
      const allRequests = res.data;

      // 3. 데이터 분리 (내가 보낸 것 vs 남이 보낸 것)
      // 백엔드 DTO(SwapRequestResponse)에 requesterId 필드 추가 필요
      // 정확한 구분을 위해 requesterId 필요
      // [임시 로직] 내 이름과 요청자 이름이 같으면 '내가 보낸 요청'으로 처리
      const myReqs = [];
      const otherReqs = [];

      allRequests.forEach((req) => {
        // 백엔드에서 requesterId를 내려준다면: if (req.requesterId === myId)
        // 현재 DTO 기준 임시 (내 이름 정보가 필요):
        if (req.requesterName === meRes.data.name) { 
          myReqs.push(req);
        } else {
          otherReqs.push(req);
        }
      });

      setSections([
        { title: "내가 보낸 요청", data: myReqs },
        { title: "받은 요청", data: otherReqs }
      ]);

    } catch (err) {
      console.error("요청 목록 로딩 실패:", err);
      Alert.alert("오류", "데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useFocusEffect(
    useCallback(() => {
      fetchRequests();
    }, [])
  );

  // 상태 정보 헬퍼
  const getStatusInfo = (status) => {
    switch (status) {
      case "OPEN": return { text: "대기중", color: "#3b82f6", bg: "#dbeafe" };
      case "ACCEPTED": return { text: "수락됨", color: "#eab308", bg: "#fef9c3" };
      case "OWNER_APPROVAL": return { text: "승인 대기", color: "#eab308", bg: "#fef9c3" };
      case "APPROVED": return { text: "완료", color: "#22c55e", bg: "#dcfce7" };
      case "DECLINED": return { text: "거절됨", color: "#ef4444", bg: "#fee2e2" };
      case "CANCELLED": return { text: "취소됨", color: "#9ca3af", bg: "#f3f4f6" };
      default: return { text: status, color: "#000", bg: "#eee" };
    }
  };

  // 수락 핸들러
  const handleAccept = async (requestId) => {
    try {
      const companyId = await AsyncStorage.getItem("currentCompanyId");
      await client.post(`/companies/${companyId}/swaps/${requestId}/accept`);
      Alert.alert("성공", "요청을 수락했습니다.", [{ text: "확인", onPress: fetchRequests }]);
    } catch (err) {
      console.error("수락 실패:", err);
      Alert.alert("실패", "요청 수락 중 오류가 발생했습니다.");
    }
  };

  const renderItem = ({ item, section }) => {
    const statusInfo = getStatusInfo(item.status);
    const isMySection = section.title === "내가 보낸 요청";

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={[styles.typeBadge, { backgroundColor: isMySection ? '#333' : '#2563eb' }]}>
            <Text style={styles.typeText}>
              {item.type === "GIVE_AWAY" ? "대타" : "교환"}
            </Text>
          </View>
          <Text style={styles.dateText}>{item.createdAt?.substring(0, 10)}</Text>
        </View>

        <Text style={styles.scheduleInfo}>{item.fromScheduleInfo}</Text>
        
        <View style={styles.divider} />

        <View style={styles.cardFooter}>
          <View>
            <Text style={styles.memberText}>
              {isMySection ? `받는 사람: ${item.targetName}` : `보낸 사람: ${item.requesterName}`}
            </Text>
          </View>
          
          <View style={[styles.statusBadge, { backgroundColor: statusInfo.bg }]}>
            <Text style={[styles.statusText, { color: statusInfo.color }]}>
              {statusInfo.text}
            </Text>
          </View>
        </View>

        {/* 받은 요청이고 상태가 OPEN이면 수락 버튼 표시 */}
        {!isMySection && item.status === 'OPEN' && (
          <TouchableOpacity 
            style={styles.acceptButton} 
            onPress={() => handleAccept(item.id)}
          >
            <Text style={styles.acceptButtonText}>수락하기</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>근무 교환 요청</Text>
        <View style={{ width: 32 }} />
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#000" style={{ marginTop: 50 }} />
      ) : (
        <SectionList
          sections={sections}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          renderSectionHeader={({ section: { title, data } }) => (
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>{title} ({data.length})</Text>
            </View>
          )}
          contentContainerStyle={{ paddingBottom: 30 }}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={() => { setRefreshing(true); fetchRequests(); }} />
          }
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Text style={styles.emptyText}>요청 내역이 없습니다.</Text>
            </View>
          }
          stickySectionHeadersEnabled={false} 
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5",
    paddingHorizontal: 20,
    paddingTop: 64,
  },
  header: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 10,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },
  sectionHeader: {
    paddingVertical: 12,
    marginTop: 10,
    marginBottom: 4,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#1f2937",
  },
  emptyContainer: {
    marginTop: 100,
    alignItems: "center",
  },
  emptyText: {
    fontSize: 16,
    color: "#9ca3af",
  },
  card: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    elevation: 2,
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 4,
    shadowOffset: { width: 0, height: 2 },
  },
  cardHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 12,
  },
  typeBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  typeText: {
    color: "#fff",
    fontSize: 11,
    fontWeight: "bold",
  },
  dateText: {
    color: "#9ca3af",
    fontSize: 12,
  },
  scheduleInfo: {
    fontSize: 16,
    fontWeight: "600",
    color: "#111",
    marginBottom: 12,
  },
  divider: {
    height: 1,
    backgroundColor: "#f3f4f6",
    marginBottom: 12,
  },
  cardFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  memberText: {
    fontSize: 14,
    color: "#4b5563",
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    fontWeight: "bold",
  },
  acceptButton: {
    marginTop: 14,
    backgroundColor: "#2563eb",
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: "center",
  },
  acceptButtonText: {
    color: "#fff",
    fontSize: 14,
    fontWeight: "bold",
  }
});