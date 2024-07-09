<template>
  <div>
    <h4>Daten von Quarkus Backend</h4>
    <h5>GET</h5>
    <div v-if="loading">Laden...</div>
    <div v-else-if="error">{{ error }}</div>
    <div v-else>
      <ul>
        <li v-for="(item, index) in data" :key="index">{{ item }}</li>
      </ul>
    </div>
    <h5>POST</h5>
    <div>
      <input v-model="inputData" type="text" placeholder="Gib etwas ein">
      <button @click="sendData">Daten senden</button>
      <div v-if="responseData">{{ responseData }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted} from 'vue';
import axios from 'axios';

const inputData = ref('');
const responseData = ref('');
const sendData = async () => {
  try {
    const response = await axios.post('http://localhost:8080/test/add', inputData.value);
    console.log('Erfolgreich gesendet:', response.data);
    responseData.value = response.data;
    data.value.push(response.data)
    // Hier kannst du die Rückgabe des Servers verarbeiten, falls erforderlich
  } catch (error) {
    console.error('Fehler beim Senden der Daten:', error);
  }
};

const data = ref<string[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// Funktion zum Abrufen der Daten vom Backend
const getData = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get('http://localhost:8080/test/get');
    data.value = response.data; // Annahme: response.data ist ein Array von Strings
  } catch (err) {
    error.value = 'Fehler beim Abrufen der Daten';
  } finally {
    loading.value = false;
  }
};


// Daten abrufen und Polling starten, wenn die Komponente montiert wird
onMounted(() => {
  getData();
});
</script>

<style scoped>
/* Optional: Hier können Stile hinzugefügt werden */
</style>
