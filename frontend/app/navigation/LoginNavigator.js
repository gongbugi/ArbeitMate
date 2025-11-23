import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import LoginScreen from "../screens/LoginScreen";
import WorkplaceAddScreen from "../screens/WorkplaceAddScreen";
import WorkplaceJoinScreen from "../screens/WorkplaceJoinScreen";
import WorkplaceRegisterScreen from "../screens/WorkplaceRegisterScreen";
import WorkplaceSelectScreen from "../screens/WorkplaceSelectScreen";


const Stack = createNativeStackNavigator();

export default function LoginNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="LoginScreen" component={LoginScreen} />
      <Stack.Screen name="WorkplaceAddScreen" component={WorkplaceAddScreen} />
      <Stack.Screen name="WorkplaceJoinScreen" component={WorkplaceJoinScreen} />
      <Stack.Screen name="WorkplaceRegisterScreen" component={WorkplaceRegisterScreen} />
      <Stack.Screen name="WorkplaceSelectScreen" component={WorkplaceSelectScreen} />
      {/* 필요 스크린 계속 추가 */}
    </Stack.Navigator>
  );
}