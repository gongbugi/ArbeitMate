import React, { useState, useEffect } from "react";
import { View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  Dimensions,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import client from "../services/api";
import AsyncStorage from '@react-native-async-storage/async-storage';

const { width } = Dimensions.get("window");
const LAYOUT_PADDING = 24;
const ITEM_SPACING = 16;
const CARD_WIDTH = (width - ITEM_SPACING - (LAYOUT_PADDING * 2)) / 2;

export default function WorkplaceSelectScreen({ navigation, route, setRole }) {
  const [workplaces, setWorkplaces] = useState([]);
  
  const fetchWorkplaces = async () => {
    try {
      const res = await client.get("/companies/me"); 
      setWorkplaces(res.data);
    } catch (err) {
      console.log("근무지 조회 실패:", err);
    }
  };

  useEffect(() => {
    fetchWorkplaces();
  }, [route.params?.refresh]);

  const handleSelectWorkplace = async (workplace) => {
    try {
      await AsyncStorage.setItem("currentCompanyId", String(workplace.companyId));
      await AsyncStorage.setItem("currentCompanyName", workplace.companyName);

      if(workplace.role === "OWNER") {
        setRole("employer");
      } else {
        setRole("worker")
      }
    } catch (e) {
      console.error("매장 정보 저장 실패", e);
    }
  }

  const dataToRender = [
    ...workplaces, 
    { id: "ADD_BUTTON", type: "ADD" }
  ];

  const renderItem = ({ item }) => {
    // 1. '추가 버튼' 렌더링
    if (item.type === "ADD") {
      return (
        <TouchableOpacity
          style={[styles.card, styles.addCard]}
          onPress={() => navigation.navigate("WorkplaceRegisterScreen")}
        >
          <Ionicons name="add" size={48} color="#555" />
          <Text style={styles.workplaceRole}>신규등록</Text>
        </TouchableOpacity>
      );
    }

    // 2. '근무지 카드' 렌더링
    return (
      <TouchableOpacity
        style={styles.card}
        onPress={() => handleSelectWorkplace(item)}
      >
        <Text style={styles.workplaceName}>{item.companyName}</Text>
        <Text style={styles.workplaceRole}>
          {item.role === "OWNER" ? "고용주" : "근무자"}
        </Text>
      </TouchableOpacity>
    );
  };

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerText}>근무지</Text>
      </View>

      {/* Grid List */}
      <FlatList
        data={dataToRender}
        keyExtractor={(item) => item.companyId || item.id}
        numColumns={2} // 2열 종대 설정
        columnWrapperStyle={styles.row} // 열 사이 간격 스타일
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        renderItem={renderItem}
      />


    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F4F4F5",
    paddingTop: 80,
    paddingHorizontal: LAYOUT_PADDING,
  },

  header: {
    marginBottom: 30,
    alignItems: 'center',
  },

  headerText: {
    fontSize: 32,
    fontWeight: "bold",
    color: "#000",
  },

  listContent: {
    paddingBottom: 40,
  },
  row: {
    justifyContent: "space-between", // 카드 사이 균등 배분
    marginBottom: 16, // 행 사이 간격
  },


  card: {
    width: CARD_WIDTH,
    height: CARD_WIDTH,
    backgroundColor: "#fff",
    borderRadius: 24,
    alignItems: "center",
    justifyContent: "center",

    //그림자
    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 6,
    elevation: 3,
  },

  addCard: {
    backgroundColor: "#F3F0F7",
  },

  workplaceName: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
    marginBottom: 4,
    textAlign: "center",
  },
  workplaceRole: {
    fontSize: 14,
    color: "#666",
    fontWeight: "500",
  },
});
