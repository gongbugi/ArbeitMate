const BASE_URL = "https://arbeitmate-api.com"; // 실제 서버 주소로 바꿔

export async function loginApi(loginId, password) {
  try {
    const res = await fetch(`${BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ loginId, password })
    });

    if (!res.ok) {
      throw new Error("로그인 실패");
    }

    const data = await res.json();
    return data;

  } catch (err) {
    console.log("로그인 오류:", err);
    throw err;
  }
}
