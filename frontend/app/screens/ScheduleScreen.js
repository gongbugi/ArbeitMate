import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { ArrowLeft } from "lucide-react-native";

export default function ScheduleScreen({ navigation }) {
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const dates = [
    ["28", "29", "30", "31", "01", "02", "03"],
    ["04", "05", "06", "07", "08", "09", "10"],
    ["11", "12", "13", "14", "15", "16", "17"],
    ["18", "19", "20", "21", "22", "23", "24"],
    ["25", "26", "27", "28", "29", "30", "01"],
  ];

  const workTime = {
    "04": "12:00–14:00",
    "11": "12:00–14:00",
    "18": "12:00–14:00",
    "25": "12:00–14:00",
  };

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <ArrowLeft size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무표</Text>
      </View>

      {/* Month */}
      <View style={styles.monthWrapper}>
        <Text style={styles.monthText}>10월</Text>
      </View>

      {/* Day names */}
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

      {/* Calendar */}
      <View>
        {dates.map((week, wIdx) => (
          <View key={wIdx} style={styles.weekRow}>
            {week.map((d, idx) => {
              const isWork = workTime[d];

              return (
                <View key={idx} style={styles.dayBox}>
                  <Text
                    style={[
                      styles.dateText,
                      idx === 0 && styles.sunday,
                      idx === 6 && styles.saturday,
                    ]}
                  >
                    {d}
                  </Text>

                  {isWork && (
                    <View style={styles.workBadge}>
                      <Text style={styles.workText}>{isWork}</Text>
                    </View>
                  )}
                </View>
              );
            })}
          </View>
        ))}
      </View>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f4f4f5", // zinc-100
    paddingHorizontal: 20,
    paddingTop: 64,
  },

  /* ------ Header ------ */
  header: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 24,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: "bold",
    color: "#000",
    marginLeft: 16,
  },

  /* ------ Month ------ */
  monthWrapper: {
    justifyContent: "center",
    flexDirection: "row",
    marginBottom: 16,
  },
  monthText: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#000",
  },

  /* ------ Day names ------ */
  daysRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 8,
  },
  dayText: {
    width: 40,
    textAlign: "center",
    fontWeight: "bold",
    color: "#000",
  },
  sunday: {
    color: "#ef4444",
  },
  saturday: {
    color: "#2563eb",
  },

  /* ------ Calendar ------ */
  weekRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 16,
  },
  dayBox: {
    alignItems: "center",
    width: 40,
  },
  dateText: {
    fontSize: 18,
    fontWeight: "bold",
    color: "#000",
  },

  /* ------ Work badge ------ */
  workBadge: {
    backgroundColor: "#fecaca",
    borderRadius: 4,
    marginTop: 4,
    paddingHorizontal: 4,
    paddingVertical: 2,
  },
  workText: {
    fontSize: 8,
    color: "#000",
    textAlign: "center",
  },
});
