import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from "@expo/vector-icons";

export default function WorkplaceJoinScreen({ navigation , setRole }) {
  
  const [code, setCode] = useState('');

  const handleJoin = () => {
    setRole("worker");
    if (!code.trim()) {
      alert('코드를 입력해주세요.');
      return;
    }
    alert(`근무지 코드 "${code}"로 가입 요청을 보냈습니다.`);
    navigation.navigate('WorkplaceSelect');
  };

  return (
    <View style={styles.container}>

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back-outline" size={32} color="#000" />
        </TouchableOpacity>

        <Text style={styles.headerTitle}>근무지 가입</Text>
        <View style={{ width: 32 }} />
      </View>

      {/* Input */}
      <Text style={styles.label}>근무지 코드</Text>
      <TextInput
        placeholder="코드를 입력하세요"
        placeholderTextColor="#A1A1AA"
        value={code}
        onChangeText={setCode}
        style={styles.input}
      />

      {/* Button */}
      <TouchableOpacity style={styles.button} onPress={handleJoin}>
        <Text style={styles.buttonText}>가입 요청</Text>
      </TouchableOpacity>

    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F4F4F5', // gray-100 느낌
    paddingHorizontal: 24,
    paddingTop: 64,
  },

  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 40,
  },

  headerTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#000',
  },

  label: {
    fontSize: 18,
    fontWeight: '600',
    color: '#000',
    marginBottom: 8,
  },

  input: {
    backgroundColor: '#fff',
    borderRadius: 24, 
    paddingHorizontal: 20,
    paddingVertical: 14,
    fontSize: 18,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    marginBottom: 30,

    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 2,
  },

  button: {
    backgroundColor: '#000',
    borderRadius: 24,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 12,
  },

  buttonText: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
  },
});
