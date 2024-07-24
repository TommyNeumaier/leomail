<script setup lang="ts">

import HeaderComponent from "@/components/HeaderComponent.vue";
import {onMounted, ref} from "vue";
import * as http from "node:http";
import axios from "axios";
import {useAuthStore} from "@/stores/auth.store";

const jwt = ref("");
const authStore = useAuthStore();

const getJwt = () => {
  axios.get("http://localhost:5173/api/auth/jwt", {
    headers: {
      Authorization: "Bearer " + authStore.$state.accessToken
    }
  }).then((response) => {
    jwt.value = JSON.stringify(response.data, null, 2);
  }).catch((error) => {
    jwt.value = error.message;
  });
};

onMounted(() =>
getJwt()
)
</script>

<template>
  <header-component></header-component>

  <div>
    <h2 style="text-align: center; margin: 5vh 0;">JWT Details</h2>
    <textarea v-model="jwt" disabled style="font-size: 2vh; width: 80vw; height: 50vh; margin: 5vh 10vw;">{{}}</textarea>
  </div>
</template>

<style scoped>

</style>