import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import E_HomeScreen from "../screens/Employer/E_HomeScreen.js";
import E_NoticeScreen from "../screens/Employer/E_NoticeScreen.js";
import E_NoticeAddScreen from "../screens/Employer/E_NoticeAddScreen.js";
import E_PayScreen from "../screens/Employer/E_PayScreen.js";
import E_ScheduleManageScreen from "../screens/Employer/E_ScheduleManageScreen.js";
import E_ShiftRequestListScreen from "../screens/Employer/E_ShiftRequestListScreen.js";
import E_ScheduleAutoAddPeriodScreen from "../screens/Employer/E_ScheduleAutoAddPeriodScreen.js";
import E_ScheduleAutoAddPeriodSelectScreen from "../screens/Employer/E_ScheduleAutoAddPeriodSelectScreen.js";
import E_ScheduleAutoAddScreen from "../screens/Employer/E_ScheduleAutoAddScreen.js";
import E_ScheduleAutoAddSummaryScreen from "../screens/Employer/E_ScheduleAutoAddSummaryScreen.js";
import E_ScheduleAutoAddWeekdayScreen from "../screens/Employer/E_ScheduleAutoAddWeekdayScreen.js";
import ScheduleScreen from "../screens/ScheduleScreen.js";
import E_InformationScreen from "../screens/Employer/E_InformationScreen";
import AddPeopleModal from "../screens/Employer/AddPeopleModal.js";
import E_ScheduleAutoAddPeopleScreen from "../screens/Employer/E_ScheduleAutoAddPeopleScreen";
import E_WorkerManageScreen from "../screens/Employer/E_WorkerManageScreen.js";
import E_WorkerTimeScreen from "../screens/Employer/E_WorkerTimeScreen.js";
import E_WorkerTimeUpdateScreen from "../screens/Employer/E_WorkerTimeUpdateScreen.js";
import E_WorkerUpdateScreen from "../screens/Employer/E_WorkerUpdateScreen.js";

const Stack = createNativeStackNavigator();

export default function E_Navigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="E_HomeScreen" component={E_HomeScreen} />
      <Stack.Screen name="E_NoticeScreen" component={E_NoticeScreen} />
      <Stack.Screen name="E_PayScreen" component={E_PayScreen} />
      <Stack.Screen name="E_NoticeAddScreen" component={E_NoticeAddScreen} />
      <Stack.Screen name="E_ScheduleManageScreen" component={E_ScheduleManageScreen} />
      <Stack.Screen name="E_ShiftRequestListScreen" component={E_ShiftRequestListScreen} />
      <Stack.Screen name="E_ScheduleAutoAddPeriodScreen" component={E_ScheduleAutoAddPeriodScreen} />
      <Stack.Screen name="E_ScheduleAutoAddPeriodSelectScreen" component={E_ScheduleAutoAddPeriodSelectScreen} />
      <Stack.Screen name="E_ScheduleAutoAddScreen" component={E_ScheduleAutoAddScreen} />
      <Stack.Screen name="E_ScheduleAutoAddSummaryScreen" component={E_ScheduleAutoAddSummaryScreen} />
      <Stack.Screen name="E_ScheduleAutoAddWeekdayScreen" component={E_ScheduleAutoAddWeekdayScreen} />
      <Stack.Screen name="ScheduleScreen" component={ScheduleScreen} />
      <Stack.Screen name="E_InformationScreen" component={E_InformationScreen} />
      <Stack.Screen name="AddPeopleModal" component={AddPeopleModal} />
      <Stack.Screen name="E_ScheduleAutoAddPeopleScreen" component={E_ScheduleAutoAddPeopleScreen} />
      <Stack.Screen name="E_WorkerManageScreen" component={E_WorkerManageScreen} />
      <Stack.Screen name="E_WorkerTimeScreen" component={E_WorkerTimeScreen} />
      <Stack.Screen name="E_WorkerTimeUpdateScreen" component={E_WorkerTimeUpdateScreen} />
      <Stack.Screen name="E_WorkerUpdateScreen" component={E_WorkerUpdateScreen} />
      {/* 필요 스크린 계속 추가 */}
    </Stack.Navigator>
  );
}
