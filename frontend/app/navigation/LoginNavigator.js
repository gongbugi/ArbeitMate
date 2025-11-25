import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import LoginScreen from "../screens/LoginScreen";
import SignScreen from "../screens/SignScreen";
import WorkplaceAddScreen from "../screens/WorkplaceAddScreen";
import WorkplaceJoinScreen from "../screens/WorkplaceJoinScreen";
import WorkplaceRegisterScreen from "../screens/WorkplaceRegisterScreen";
import WorkplaceSelectScreen from "../screens/WorkplaceSelectScreen";


const Stack = createNativeStackNavigator();

export default function LoginNavigator({setRole}) {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="LoginScreen" component={LoginScreen} />
      <Stack.Screen name="SignScreen" component={SignScreen} />
      <Stack.Screen name="WorkplaceAddScreen" component={WorkplaceAddScreen} />
      <Stack.Screen name="WorkplaceJoinScreen" component={WorkplaceJoinScreen} />
      <Stack.Screen name="WorkplaceRegisterScreen" component={WorkplaceRegisterScreen} />
      <Stack.Screen name="WorkplaceSelectScreen">
        {(props) => <WorkplaceSelectScreen {...props} setRole={setRole} />}
      </Stack.Screen>
      {/* 필요 스크린 계속 추가 */}
    </Stack.Navigator>
  );
}