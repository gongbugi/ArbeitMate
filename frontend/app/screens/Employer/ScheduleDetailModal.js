import React from "react";
import { View, Text, Modal, TouchableOpacity, StyleSheet } from "react-native";

export default function ScheduleDetailModal({ visible, onClose, date, slots }) {
    if (!date) return null;

    return (
        <Modal transparent visible={visible} animationType="fade">
            <View style={styles.overlay}>
                <View style={styles.modal}>
                    
                    <TouchableOpacity style={styles.closeBtn} onPress={onClose}>
                        <Text style={{ fontSize: 18 }}>✕</Text>
                    </TouchableOpacity>

                    <Text style={styles.title}>
                        {new Date(date).getMonth() + 1}월{" "}
                        {new Date(date).getDate()}일
                    </Text>

                    {slots.length === 0 && (
                        <Text style={{ textAlign: "center", marginTop: 10 }}>
                            근무 없음
                        </Text>
                    )}

                    {slots.map((slot, idx) => (
                        <View key={idx} style={styles.slotBox}>
                            <Text>근무자: {slot.workers?.[0]?.memberName ?? "미배정"}</Text>
                            <Text>담당 업무: {slot.roleName}</Text>
                            <Text>
                                근무 시간: {slot.startTime.slice(0,5)} ~ {slot.endTime.slice(0,5)}
                            </Text>
                        </View>
                    ))}
                </View>
            </View>
        </Modal>
    );
}

const styles = StyleSheet.create({
    overlay: {
        flex: 1,
        backgroundColor: "rgba(0,0,0,0.4)",
        justifyContent: "center",
        alignItems: "center",
    },
    modal: {
        width: "80%",
        backgroundColor: "white",
        borderRadius: 15,
        padding: 20,
    },
    closeBtn: {
        alignSelf: "flex-end",
    },
    title: {
        fontSize: 20,
        fontWeight: "bold",
        marginBottom: 15,
    },
    slotBox: {
        marginVertical: 8,
        padding: 10,
        backgroundColor: "#f3f3f3",
        borderRadius: 8,
    },
});
