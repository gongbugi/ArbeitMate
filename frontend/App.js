import React, { useState } from "react";
import { NavigationContainer } from "@react-navigation/native";

import LoginNavigator from "./app/navigation/LoginNavigator";
import E_Navigator from "./app/navigation/E_Navigator";
import W_Navigator from "./app/navigation/W_Navigator";

export default function App() {
  const [role, setRole] = useState(null); 
  // role: null → 로그인 못함
  //       none → 근무지 없음 (LoginNavigator 내부에서 처리)
  //       employer → 고용주
  //       worker → 근무자

  return (
    <NavigationContainer>

      {/* 로그인 or 근무지 선택/생성 단계 (role === null 이면 LoginNavigator 사용) */}
      {role === null && (
        <LoginNavigator setRole={setRole} />
        //<E_Navigator />
        //<W_Navigator />
      )}

      {/* 고용주 */}
      {role === "employer" && (
        <E_Navigator />
      )}

      {/* 근무자 */}
      {role === "worker" && (
        <W_Navigator />
      )}

    </NavigationContainer>
  );
}
