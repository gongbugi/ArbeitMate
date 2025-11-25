import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft, ChevronRight } from "lucide-react-native";
import axios from "axios";

const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function WorkerManageScreen({ navigation }) {
    const [workers, setWorkers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadWorkers();
    }, []);

    const loadWorkers = async () => {
        try {
            const res = await axios.get(`${BASE_URL}/workers`);
            setWorkers(res.data);
        } catch (err) {
            console.log("근무자 목록 불러오기 오류:", err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <ActivityIndicator size="large" style={{ marginTop: 100 }} />;
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

            {/* Card */}
            <View style={styles.card}>

                {/* 김XX */}
                <View style={styles.rowItem}>
                    <TouchableOpacity
                        style={styles.editBtn}
                        onPress={() => navigation.navigate("E_WorkerUpdateScreen")}>
                        <Text style={styles.nameText}>김XX</Text>
                        <ChevronRight size={32} color="#999" />
                    </TouchableOpacity>
                </View>


            </View>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#E5E5E5",
        paddingHorizontal: 24,
        paddingTop: 60,
    },

    /* Header */
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: 40,
    },
    headerTitle: {
        fontSize: 24,
        fontWeight: "bold",
        color: "#000",
    },

    /* White Card */
    card: {
        backgroundColor: "#fff",
        borderRadius: 24,
        paddingVertical: 30,
        paddingHorizontal: 20,
    },

    /* Row */
    rowItem: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        paddingVertical: 20,
    },
    nameText: {
        fontSize: 26,
        fontWeight: "bold",
        color: "#000",
    },

    /* Edit Button */
    editBtn: {
        width: "100%",
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
    },
    editIcon: {
        width: 4,
        height: 28,
        backgroundColor: "#999",
    },
});
