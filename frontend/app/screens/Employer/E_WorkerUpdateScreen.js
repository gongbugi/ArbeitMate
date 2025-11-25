import React from "react";
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from "react-native";
import { ArrowLeft } from "lucide-react-native";
import axios from "axios";

const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function E_WorkerUpdateScreen({ navigation }) {
    const { workerId } = route.params;
    const [worker, setWorker] = useState(null);

    useEffect(() => {
        loadWorker();
    }, []);

    const loadWorker = async () => {
        try {
            const res = await axios.get(`${BASE_URL}/worker/${workerId}`);
            setWorker(res.data);
        } catch (err) {
            console.log("근무자 상세 정보 오류:", err);
        }
    };

    const saveWorker = async () => {
        try {
            await axios.put(`${BASE_URL}/worker/${workerId}`, worker);
            alert("저장되었습니다.");
            navigation.goBack();
        } catch (err) {
            console.log("근무자 정보 저장 오류:", err);
        }
    };

    if (!worker) return <ActivityIndicator size="large" style={{ marginTop: 100 }} />;
    return (
        <View style={styles.container}>

            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()}>
                    <ArrowLeft size={28} color="#000" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>근무자 관리</Text>
                <View style={{ width: 28 }} />
            </View>

            <ScrollView showsVerticalScrollIndicator={false}>

                {/* 이름 */}
                <Text style={styles.sectionLabel}>이름</Text>
                <View style={styles.box}>
                    <Text style={styles.boxText}>김XX</Text>
                </View>

                {/* 담당 업무 */}
                <Text style={styles.sectionLabel}>담당 업무</Text>
                <View style={styles.box}>
                    <Text style={styles.boxText}>서빙</Text>
                </View>

                {/* 시급 */}
                <Text style={styles.sectionLabel}>시급</Text>
                <View style={styles.box}>
                    <View style={styles.rowBetween}>
                        <Text style={styles.boxText}>12,500</Text>
                        <Text style={styles.unitText}>원</Text>
                    </View>
                </View>

                {/* 고정 근무 시간 */}
                <Text style={styles.sectionLabel}>고정 근무 시간</Text>
                <View style={styles.boxRow}>
                    <Text style={styles.boxText}>월 12:00 - 14:00</Text>
                    <TouchableOpacity style={styles.smallIcon}
                        onPress={() => navigation.navigate("E_WorkerTimeScreen")} />
                </View>

                {/* 근무 가능 시간 */}
                <Text style={styles.sectionLabel}>근무 가능 시간</Text>
                <View style={styles.boxLarge}>
                    <Text style={styles.grayText}>월 12:00 - 14:00</Text>
                    <Text style={styles.grayText}>화 12:00 - 14:00</Text>
                </View>

            </ScrollView>

            {/* 저장 버튼 */}
            <TouchableOpacity style={styles.saveBtn}>
                <Text style={styles.saveText}>저장</Text>
            </TouchableOpacity>

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

    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 40,
    },
    headerTitle: {
        fontSize: 22,
        fontWeight: "bold",
        color: "#000",
    },

    /* Section label */
    sectionLabel: {
        fontSize: 18,
        fontWeight: "600",
        color: "#000",
        marginBottom: 8,
        marginTop: 16,
    },

    /* Basic Box */
    box: {
        backgroundColor: "#fff",
        height: 52,
        borderRadius: 28,
        justifyContent: "center",
        paddingHorizontal: 20,
    },
    boxText: {
        fontSize: 18,
        color: "#000",
        fontWeight: "500",
    },

    /* Row box (for 고정 근무 시간) */
    boxRow: {
        backgroundColor: "#fff",
        height: 52,
        borderRadius: 28,
        paddingHorizontal: 20,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
    },

    /* Large text box (근무 가능 시간) */
    boxLarge: {
        backgroundColor: "#fff",
        borderRadius: 28,
        paddingVertical: 16,
        paddingHorizontal: 20,
    },

    rowBetween: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
    },

    unitText: {
        fontSize: 18,
        fontWeight: "500",
        color: "#000",
    },

    grayText: {
        fontSize: 16,
        color: "rgba(0,0,0,0.4)",
        marginBottom: 6,
        fontWeight: "500",
    },

    /* small icon placeholder */
    smallIcon: {
        width: 14,
        height: 24,
        backgroundColor: "#ccc",
        borderRadius: 4,
    },

    /* Save Button */
    saveBtn: {
        backgroundColor: "#000",
        height: 60,
        borderRadius: 32,
        justifyContent: "center",
        alignItems: "center",
        marginBottom: 20,
    },
    saveText: {
        color: "#fff",
        fontSize: 22,
        fontWeight: "bold",
    },
});
