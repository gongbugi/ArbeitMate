import React, { useEffect, useState } from "react";
import {
    View,
    Text,
    TouchableOpacity,
    ScrollView,
    StyleSheet,
} from "react-native";
import { ArrowLeft } from "lucide-react-native";
import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";
import ScheduleDetailModal from "./ScheduleDetailModal";

const BASE_URL = "http://<백엔드-서버-IP>:8080";

export default function E_ScheduleMainScreen({ navigation }) {
    const [monthlyData, setMonthlyData] = useState([]);
    const [calendar, setCalendar] = useState([]);
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedSlots, setSelectedSlots] = useState([]);
    const [modalVisible, setModalVisible] = useState(false);

    const year = 2025;
    const month = 10;

    const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    useEffect(() => {
        generateCalendar(year, month);
        loadMonthlyData();
    }, []);

    /* 월 근무표 불러오기 */
    const loadMonthlyData = async () => {
        try {
            const companyId = await AsyncStorage.getItem("currentCompanyId");
            const myMemberId = await AsyncStorage.getItem("myMemberId"); // ★ 내 ID

            if (!companyId) return;

            const res = await axios.get(
                `${BASE_URL}/companies/${companyId}/schedule/monthly`,
                { params: { year, month } }
            );

            // ★ 내 근무 여부 추가
            const processed = res.data.map((slot) => {
                const isMine =
                    slot.workers?.some((w) => w.memberId === myMemberId) ?? false;
                return { ...slot, isMine };
            });

            setMonthlyData(processed);
        } catch (err) {
            console.log("ERROR: ", err);
        }
    };

    /* 달력 생성 */
    const generateCalendar = (year, month) => {
        const start = new Date(year, month - 1, 1);
        const end = new Date(year, month, 0);

        const startDay = start.getDay();
        const totalDays = end.getDate();

        let weeks = [];
        let week = [];

        // 앞 공백 넣기
        for (let i = 0; i < startDay; i++) week.push(null);

        // 날짜 넣기
        for (let d = 1; d <= totalDays; d++) {
            week.push(d);
            if (week.length === 7) {
                weeks.push(week);
                week = [];
            }
        }

        // 마지막 줄 마무리
        while (week.length < 7) week.push(null);
        weeks.push(week);

        setCalendar(weeks);
    };

    /* 날짜 클릭 */
    const onPressDate = (day) => {
        if (!day) return;

        const dateString = `${year}-${String(month).padStart(2, "0")}-${String(
            day
        ).padStart(2, "0")}`;

        const slots = monthlyData.filter((slot) => slot.workDate === dateString);

        setSelectedDate(dateString);
        setSelectedSlots(slots);
        setModalVisible(true);
    };

    return (
        <View style={styles.container}>
            {/* HEADER */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()}>
                    <ArrowLeft size={32} color="#000" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>근무표</Text>
            </View>

            {/* MONTH */}
            <View style={styles.monthWrapper}>
                <Text style={styles.monthText}>{month}월</Text>
            </View>

            {/* DAYS OF WEEK */}
            <View style={styles.daysRow}>
                {days.map((d, idx) => (
                    <Text
                        key={idx}
                        style={[
                            styles.dayText,
                            idx === 0 && styles.sunday,
                            idx === 6 && styles.saturday,
                        ]}
                    >
                        {d}
                    </Text>
                ))}
            </View>

            {/* CALENDAR */}
            <ScrollView>
                <View style={styles.calendarBox}>
                    {calendar.map((week, wi) => (
                        <View key={wi} style={styles.weekRow}>
                            {week.map((day, di) => {

                                // 빈 칸
                                if (!day)
                                    return (
                                        <View
                                            key={di}
                                            style={{ width: 45, height: 60 }}
                                        />
                                    );

                                const dateStr = `${year}-${String(month).padStart(
                                    2,
                                    "0"
                                )}-${String(day).padStart(2, "0")}`;

                                // 해당 날짜의 근무 slot
                                const slot = monthlyData.find((s) => s.workDate === dateStr);

                                return (
                                    <TouchableOpacity
                                        key={di}
                                        style={styles.dayCell}
                                        onPress={() => onPressDate(day)}
                                    >
                                        {/* 날짜 텍스트 */}
                                        <Text
                                            style={[
                                                styles.dateText,
                                                di === 0 && styles.sunday,
                                                di === 6 && styles.saturday,
                                            ]}
                                        >
                                            {String(day).padStart(2, "0")}
                                        </Text>

                                        {/* 근무가 있을 때 */}
                                        {slot && (
                                            <View
                                                style={[
                                                    styles.workBadge,
                                                    slot.isMine ? styles.myWork : styles.otherWork,
                                                ]}
                                            >
                                                <Text style={styles.workText}>
                                                    {slot.startTime.slice(0, 5)} - {slot.endTime.slice(0, 5)}
                                                </Text>
                                            </View>
                                        )}
                                    </TouchableOpacity>
                                );
                            })}
                        </View>
                    ))}

                </View>
            </ScrollView>

            {/* 상세 모달 */}
            <ScheduleDetailModal
                visible={modalVisible}
                onClose={() => setModalVisible(false)}
                date={selectedDate}
                slots={selectedSlots}
            />
        </View>
    );
}



const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#F4F4F5",
        paddingTop: 60,
        paddingHorizontal: 16,
    },

    header: {
        flexDirection: "row",
        alignItems: "center",
        marginBottom: 20,
    },
    headerTitle: {
        marginLeft: 16,
        fontSize: 20,
        fontWeight: "bold",
        color: "#000",
    },

    monthWrapper: {
        alignItems: "center",
        marginBottom: 12,
    },
    monthText: {
        fontSize: 20,
        fontWeight: "bold",
    },

    daysRow: {
        flexDirection: "row",
        justifyContent: "space-between",
        paddingHorizontal: 8,
        marginBottom: 8,
    },
    dayText: {
        width: 45,
        textAlign: "center",
        fontWeight: "bold",
    },
    sunday: { color: "#EF4444" },
    saturday: { color: "#2563EB" },

    calendarBox: {
        backgroundColor: "#fff",
        borderRadius: 16,
        paddingVertical: 20,
        paddingHorizontal: 10,
    },

    weekRow: {
        flexDirection: "row",
        justifyContent: "space-between",
        marginBottom: 20,
    },

    dayCell: {
        width: 45,
        alignItems: "center",
    },

    dateText: {
        fontSize: 16,
        fontWeight: "600",
    },

    workBadge: {
        marginTop: 6,
        paddingVertical: 2,
        paddingHorizontal: 4,
        borderRadius: 4,
    },
    myWork: {
        backgroundColor: "#BBF7D0", // 초록
    },
    otherWork: {
        backgroundColor: "#FECACA", // 빨강
    },
    workText: {
        fontSize: 8,
        fontWeight: "600",
        color: "#000",
    },
});
