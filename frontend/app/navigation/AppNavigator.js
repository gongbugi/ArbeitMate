import React, { useState } from "react";
import { NavigationContainer } from "@react-navigation/native";
import EmployerNavigator from "./EmployerNavigator";
import EmployeeNavigator from "./EmployeeNavigator";
import LoginScreen from "../screens/LoginScreen";

export default function AppNavigator() {
  const [role, setRole] = useState(null);

  // 로그인 성공 후 role 저장하는 로직 필요
  const handleLogin = (userRole) => setRole(userRole);

  return (
    <NavigationContainer>
      {role === null ? (
        <LoginScreen onLogin={handleLogin} />
      ) : role === "employer" ? (
        <EmployerNavigator />
      ) : (
        <EmployeeNavigator />
      )}
    </NavigationContainer>
  );
}
