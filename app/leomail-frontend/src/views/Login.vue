<script setup lang="ts">
import { ref } from 'vue';

const username = ref('');
const password = ref('');

const handleLogin = () => {
  // Add your login logic here
  console.log('Username:', username.value);
  console.log('Password:', password.value);
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