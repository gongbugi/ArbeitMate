import React, { useState, useEffect } from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import client from "../services/api";

export default function WorkplaceSelectScreen({ navigation, route }) {
  const [workplaces, setWorkplaces] = useState([]);
  
  const fetchWorkplaces = async () => {
    try {
      const res = await client.get("근무지 조회 api"); 
      setWorkplaces(res.data);
    } catch (err) {
      console.log("근무지 조회 실패:", err);
    }
  };

  useEffect(() => {
    fetchWorkplaces();
  }, [route.params?.refresh]);

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerText}>근무지</Text>
      </View>

      {/* Add Button Card */}
      <TouchableOpacity
        style={styles.card}
        onPress={() => navigation.navigate("WorkplaceRegisterScreen")}
      >
        <Ionicons name="add" size={56} color="#555" />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F4F4F5",
    paddingTop: 80,
    alignItems: "center",
  },

  header: {
    marginBottom: 40,
  },

  headerText: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#000",
  },


  card: {
    width: 150,
    height: 150,
    backgroundColor: "#fff",
    borderRadius: 24,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOpacity: 0.1,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 6,
    elevation: 4,
  },
});
