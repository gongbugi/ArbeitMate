import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import W_HomeScreen from "../screens/Worker/W_HomeScreen";
//import W_InformationScreen from "../screens/Worker/W_InformationScreen";
import W_NoticeScreen from "../screens/Worker/W_NoticeScreen";
import W_PayScreen from "../screens/Worker/W_PayScreen";
import W_ScheduleAddScreen from "../screens/Worker/W_ScheduleAddScreen";
import W_ScheduleCheckScreen from "../screens/Worker/W_ScheduleCheckScreen";
import W_ScheduleManageScreen from "../screens/Worker/W_ScheduleManageScreen";
import W_ScheduleRequestScreen from "../screens/Worker/W_ScheduleRequestScreen";
import W_ShiftListScreen from "../screens/Worker/W_ShiftListScreen";
import W_InformationScreen from "../screens/Worker/W_InformationScreen";


const Stack = createNativeStackNavigator();

export default function W_Navigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="W_Home" component={W_HomeScreen} />
      <Stack.Screen name="W_NoticeScreen" component={W_NoticeScreen} />
      <Stack.Screen name="W_PayScreen" component={W_PayScreen} />
      <Stack.Screen name="W_ScheduleCheckScreen" component={W_ScheduleCheckScreen} />
      <Stack.Screen name="W_ScheduleAddScreen" component={W_ScheduleAddScreen} />
      <Stack.Screen name="W_ScheduleManageScreen" component={W_ScheduleManageScreen} />
      <Stack.Screen name="W_ScheduleRequestScreen" component={W_ScheduleRequestScreen} />
      <Stack.Screen name="W_ShiftListScreen" component={W_ShiftListScreen} />
      <Stack.Screen name="W_InformationScreen" component={W_InformationScreen} />
      {/* 필요 스크린 계속 추가 */}
    </Stack.Navigator>
  );
}
