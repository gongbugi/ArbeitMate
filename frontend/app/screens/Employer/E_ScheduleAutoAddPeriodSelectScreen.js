import React from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Modal,
  StyleSheet,
  ScrollView,
} from "react-native";
import { X, ArrowLeft, ArrowRight } from "lucide-react-native";

export default function E_ScheduleAutoAddPeriodSelectScreen({ visible, onClose, onSelect }) {
  const days = ["S", "M", "T", "W", "T", "F", "S"];
  const dates = [
    [null, null, 1, 2, 3, 4, 5],
    [6, 7, 8, 9, 10, 11, 12],
    [13, 14, 15, 16, 17, 18, 19],
    [20, 21, 22, 23, 24, 25, 26],
    [27, 28, 29, 30, 31, null, null],
  ];

  return (
    <Modal visible={visible} transparent animationType="fade">
      {/* Dim background */}
      <View style={styles.overlay}>

        {/* White Modal Box */}
        <View style={styles.modalBox}>

          {/* Header */}
          <View style={styles.header}>
            <View>
              <Text style={styles.label}>Select date</Text>
              <Text style={styles.selectedDate}>Mon, Aug 17</Text>
            </View>

            <TouchableOpacity onPress={onClose}>
              <X size={26} color="#444" />
            </TouchableOpacity>
          </View>

          {/* Month selector */}
          <View style={styles.monthRow}>
            <TouchableOpacity>
              <ArrowLeft size={24} color="#444" />
            </TouchableOpacity>

            <Text style={styles.monthText}>August 2025</Text>

            <TouchableOpacity>
              <ArrowRight size={24} color="#444" />
            </TouchableOpacity>
          </View>

          {/* Week day labels */}
          <View style={styles.weekRow}>
            {days.map((d, i) => (
              <Text key={i} style={styles.weekText}>
                {d}
              </Text>
            ))}
          </View>

          {/* Dates */}
          <ScrollView>
            {dates.map((week, wi) => (
              <View key={wi} style={styles.dateRow}>
                {week.map((d, di) => (
                  <TouchableOpacity
                    key={di}
                    disabled={!d}
                    style={[
                      styles.dateCell,
                      d === 17 && styles.selectedDateCell,
                    ]}
                  >
                    <Text
                      style={[
                        styles.dateText,
                        d === 17 && styles.selectedDateText,
                      ]}
                    >
                      {d ? d : ""}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            ))}
          </ScrollView>

          {/* Buttons */}
          <View style={styles.footer}>
            <TouchableOpacity>
              <Text style={styles.clearBtn}>Clear</Text>
            </TouchableOpacity>

            <View style={styles.footerRight}>
              <TouchableOpacity onPress={onClose}>
                <Text style={styles.cancelBtn}>Cancel</Text>
              </TouchableOpacity>

              <TouchableOpacity onPress={() => onSelect("2025-08-17")}>
                <Text style={styles.okBtn}
                onPress={() => navigation.navigate("E_ScheduleAutoAddWeekdayScreen")}>OK</Text>
              </TouchableOpacity>
            </View>
          </View>

        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.5)",
    justifyContent: "center",
    padding: 24,
  },

  modalBox: {
    backgroundColor: "#fff",
    borderRadius: 24,
    padding: 20,
    maxHeight: "85%",
  },

  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 16,
  },
  label: {
    color: "#666",
    fontSize: 14,
  },
  selectedDate: {
    fontSize: 26,
    fontWeight: "600",
    marginTop: 6,
  },

  monthRow: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 16,
    gap: 20,
  },
  monthText: {
    fontSize: 18,
    fontWeight: "600",
  },

  weekRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  weekText: {
    width: 40,
    textAlign: "center",
    color: "#555",
    fontWeight: "bold",
  },

  dateRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 12,
  },
  dateCell: {
    width: 40,
    height: 40,
    justifyContent: "center",
    alignItems: "center",
    borderRadius: 20,
  },
  dateText: {
    fontSize: 16,
    color: "#000",
  },
  selectedDateCell: {
    backgroundColor: "#000",
  },
  selectedDateText: {
    color: "#fff",
  },

  footer: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 20,
    alignItems: "center",
  },
  clearBtn: {
    color: "#000",
    fontSize: 14,
    fontWeight: "600",
  },

  footerRight: {
    flexDirection: "row",
    gap: 16,
  },
  cancelBtn: {
    fontSize: 14,
    fontWeight: "600",
    color: "#555",
  },
  okBtn: {
    fontSize: 14,
    fontWeight: "600",
    color: "#000",
  },
});
