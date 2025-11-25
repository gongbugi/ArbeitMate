import React, { useState, useEffect } from "react";
import {
    View,
    Text,
    TouchableOpacity,
    StyleSheet,
    ScrollView,
} from "react-native";
import { ArrowLeft, Plus, ChevronRight } from "lucide-react-native";
import axios from "axios";

const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function E_WorkerTimeScreen({ navigation, route }) {
    const { workerId } = route.params;
    const [fixedTimes, setFixedTimes] = useState([]);

    useEffect(() => {
        loadTimes();
    }, []);

    const loadTimes = async () => {
        const res = await axios.get(`${BASE_URL}/worker/${workerId}/fixed-times`);
        setFixedTimes(res.data);
    };
    return (
        <View style={styles.container}>

            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()}>
                    <ArrowLeft size={28} color="#000" />
                </TouchableOpacity>

                <Text style={styles.headerTitle}>고정 근무 시간</Text>

                <TouchableOpacity onPress={() => navigation.navigate("E_WorkerTimeUpdateScreen",{ workerId })}>
                    <Text style={styles.addText}>추가</Text>
                </TouchableOpacity>
            </View>

            {/* White Card */}
            <View style={styles.card}>
                <ScrollView showsVerticalScrollIndicator={false}>

                    {/* 항목 */}
                    <View style={styles.item}>
                        <View>
                            <Text style={styles.timeText}>12:00 - 14:00</Text>
                            <Text style={styles.dayText}>월</Text>
                        </View>

                        <ChevronRight size={26} color="#000" />
                    </View>

                </ScrollView>
            </View>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#E7E7E8",
        paddingHorizontal: 24,
        paddingTop: 60,
    },

    /* Header */
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 20,
    },
    headerTitle: {
        fontSize: 22,
        fontWeight: "bold",
        color: "#000",
    },
    addText: {
        fontSize: 16,
        fontWeight: "bold",
        color: "#000",
    },

    /* White area */
    card: {
        backgroundColor: "#fff",
        borderRadius: 28,
        paddingVertical: 20,
        paddingHorizontal: 20,
        flex: 1,
    },

    /* Item */
    item: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        paddingVertical: 18,
        borderBottomWidth: 1,
        borderBottomColor: "#f0f0f0",
    },

    timeText: {
        fontSize: 26,
        fontWeight: "600",
        color: "#000",
    },
    dayText: {
        fontSize: 18,
        color: "rgba(0,0,0,0.6)",
        marginTop: 4,
    },
});
