<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import {KEYCLOAK_CLIENT_SECRET} from "@/configs/keycloak.config";

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  try {
    console.log('Starting login process');
    const response = await axios.post('https://kc.tommyneumaier.at/realms/2425-5bhitm/protocol/openid-connect/token', new URLSearchParams({
      client_id: 'leomail',
      client_secret: KEYCLOAK_CLIENT_SECRET,
      grant_type: 'password',
      username: username.value,
      password: password.value,
      scope: "openid"
    }), {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    const { access_token, refresh_token } = response.data;

    localStorage.setItem('access_token', access_token);
    localStorage.setItem('refresh_token', refresh_token);
    router.push('/').then(r => {});
  } catch (error) {
    errorMessage.value = 'Invalid username or password';
    console.error('Login failed', error);
  }
};

</script>

<template>
<div id="login-container">
  <h2>Login</h2>
  <form @submit.prevent="handleLogin">
    <div class="form-group">
      <label for="username">Benutzername</label>
      <input type="text" id="username" v-model="username" required />
    </div>
    <div class="form-group">
      <label for="password">Passwort</label>
      <input type="password" id="password" v-model="password" required />
    </div>
    <button type="submit">Login</button>
    <p v-if="errorMessage">{{ errorMessage }}</p>
  </form>
</div>
</template>

<style scoped>
#login-container{
  width: 40%;
  height: auto;
  margin: auto;
  margin-top: 10%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);;
}
form{
  padding: 10%;
}
</style>